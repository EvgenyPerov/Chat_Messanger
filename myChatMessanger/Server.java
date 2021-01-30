package myChatMessanger;

import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Server {
    private static Map<String, Connection> connectionMap = new ConcurrentHashMap<>();


    public static void main(String[] args) {

        System.out.println("Введите № порта сервера");
        int portNomer = ConsoleHelper.readInt();
        ServerSocket serverSocket = null;
        try {
            serverSocket = new ServerSocket(portNomer);
            System.out.println("Сервер запущен!");
            Socket clientSocket;
            while (true){
                clientSocket = serverSocket.accept();
                new Handler(clientSocket).start();
            }
        } catch (IOException e) {
            ConsoleHelper.writeMessage("Сервер не создан, попробуйте еще раз");
            try {
                serverSocket.close();
            } catch (IOException ioException) {
                ConsoleHelper.writeMessage("Ошибка закрытия сервера");
            }
        }
    }

    private static class Handler extends Thread{
        private Socket socket;

        private Handler(Socket socket){
            this.socket = socket;
        }

        private String serverHandshake(Connection connection) throws IOException, ClassNotFoundException{
            Message message;
            while (true) {
                connection.send(new Message(MessageType.NAME_REQUEST, "Введите ваше имя"));
                message = connection.receive();
                if (message.getType() == MessageType.USER_NAME )
                    if (!message.getData().isEmpty())
                        if (!connectionMap.containsKey(message.getData()))   break;
            }
            connectionMap.put(message.getData(), connection);
            connection.send(new Message(MessageType.NAME_ACCEPTED, "Ваше имя принято"));
            return message.getData();
        }

        private void notifyUsers(Connection connection, String userName) throws IOException {
            for (String key: connectionMap.keySet())  {
                if (key != userName)
                connection.send(new Message(MessageType.USER_ADDED, key));
            }
        }

        private void serverMainLoop(Connection connection, String fromUserName) throws IOException, ClassNotFoundException {
           Message message;
           while (true) {
               message = connection.receive();
               if (message.getType() == MessageType.TEXT) {
                   String txt = fromUserName + ": " + message.getData();
                   //*****  проверка ненормативной лексики и бан *******//
  //                 if (bannedWords(message.getData()) == false)
                   sendBroadcastMessage(new Message(MessageType.TEXT, txt));
 //                  else
 //                      sendPrivateMessage(new Message(MessageType.PRIVATE_TEXT,"exit", message.getToUserName()));
               } else
               if (message.getType() == MessageType.PRIVATE_TEXT) {
                   String toUser = message.getToUserName();
                   String txt = fromUserName + " лично для "+ toUser+ ": " + message.getData();
                   boolean isSendPrivate = sendPrivateMessage(new Message(MessageType.PRIVATE_TEXT,txt,toUser));
                   if (isSendPrivate== false)
                       connection.send(new Message(MessageType.TEXT, "Такого пользователя в чате нет \n"+
                               "Напишите \"Личное сообщение для\" имя_пользователя \n" +
                                "(для графического пользователя - нажать галочку \"Личное сообщение\" и выбрать)"));
               }  else
               if (message.getType() == MessageType.PRIVATE_FILE) {
                   File file =  message.getFile();
                   String toUser = message.getToUserName();
                   String txt = "От "+ fromUserName + " личный файл для "+ toUser+ ": ....";
                   ConsoleHelper.writeMessage(txt);  // удалить. не определяет toUser
                   boolean isSendPrivateFile = sendPrivateMessage(new Message(MessageType.PRIVATE_FILE, file,toUser));

                   if (isSendPrivateFile == false) {

                       connection.send(new Message(MessageType.TEXT, "Такого пользователя в чате нет или файла \n" +
                               "Напишите \"Личный файл для\" имя_пользователя Файл_с_путем" +
                               "(для графического пользователя - выбрать в меню Отправить файл и выбрать получателя)"));
                   }
                   else
                       sendPrivateMessage(new Message(MessageType.PRIVATE_TEXT, "Получение файла от " +
                               fromUserName, toUser));
               }
               else
                   ConsoleHelper.writeMessage("Ошибка - неизвестный тип сообщения");
           }
        }

        public void run() {
            ConsoleHelper.writeMessage("Установлено новое соединение с " + socket.getRemoteSocketAddress());
            String userName = null;
            try (Connection connection = new Connection(socket)) {
                userName = serverHandshake(connection);

                sendBroadcastMessage(new Message(MessageType.USER_ADDED, userName));
                notifyUsers(connection, userName);
                serverMainLoop(connection, userName); // цикл обмена сообщениями выполняется пока не выйдешь
            } catch (IOException | ClassNotFoundException e) {
                    ConsoleHelper.writeMessage("Произошла ошибка при обмене данными с удаленным адресом");
            }

            if (userName != null) {
                connectionMap.remove(userName);
                sendBroadcastMessage(new Message(MessageType.USER_REMOVED, userName));
                ConsoleHelper.writeMessage("Соединение с удаленным адресом закрыто");
            }

        }

    }     // конец класса Handler

    public static void sendBroadcastMessage(Message message){
        for (Map.Entry<String, Connection> pair : connectionMap.entrySet()){
            try {
                pair.getValue().send(message);
            } catch (IOException e) {
                ConsoleHelper.writeMessage("Ошибка отправки сообщения всем");
            }
        }
    }

    public static boolean  sendPrivateMessage(Message message){
        String toUser = message.getToUserName();
//                   ConsoleHelper.writeMessage("Имя получателя ПОСЛЕ ="+toUser); // убрать
//                    connectionMap.get(toUser);                             // убрать
        if (connectionMap.containsKey(toUser)) {
            for (Map.Entry<String, Connection> pair : connectionMap.entrySet()){
                try {
                    if (pair.getKey().equalsIgnoreCase(toUser)) {
                        pair.getValue().send(message);
                    }
                } catch (IOException e) {
                    ConsoleHelper.writeMessage("Ошибка отправки приватного сообщения/файла");
                }
            }
            return true;
        }    else {
            ConsoleHelper.writeMessage("Такого пользователя в чате нет");
            return false;
        }
    }

}
