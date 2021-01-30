package myChatMessanger.client;

import myChatMessanger.Connection;
import myChatMessanger.ConsoleHelper;
import myChatMessanger.Message;
import myChatMessanger.MessageType;

import java.io.File;
import java.io.IOException;
import java.net.Socket;

public class Client {
    protected Connection connection;
    protected volatile boolean clientConnected = false;

    public static void main(String[] args){
        Client client = new Client();
        client.run();
    }


    public class SocketThread extends Thread {
        Socket socket;

        public void run() {
            try {
                socket = new Socket(getServerAddress(),getServerPort());
                connection = new Connection(socket);
                clientHandshake();
                clientMainLoop();
            } catch (IOException | ClassNotFoundException e) {
                notifyConnectionStatusChanged(false);
            }
        }

        protected void clientHandshake() throws IOException, ClassNotFoundException{
            Message message;
            while (true){
                message = connection.receive();
                if (message.getType() == MessageType.NAME_REQUEST)
                    connection.send(new Message(MessageType.USER_NAME,getUserName()));
                else
                    if (message.getType() == MessageType.NAME_ACCEPTED) {
                    notifyConnectionStatusChanged(true);
                    break;
                    }
                    else
                        throw new IOException("Unexpected MessageType");
            }
        }

        protected void clientMainLoop() throws IOException, ClassNotFoundException{
            Message message;
            while (true){
                message = connection.receive();
                MessageType messageType = message.getType();
                String messageTxt =message.getData();
                if (messageType != null)
                    switch (messageType){
                        case TEXT: processIncomingMessage(messageTxt);
                                   break;
                        case USER_ADDED: informAboutAddingNewUser(messageTxt);
                                    break;
                        case USER_REMOVED: informAboutDeletingNewUser(messageTxt);
                                    break;
                        case PRIVATE_TEXT: processIncomingMessage(messageTxt);
                            break;
                        case PRIVATE_FILE: processIncomingFile(message.getFile());  //проверить здесь
                            break;
                        default: throw new IOException("Unexpected MessageType");
                    } else throw new IOException("Unexpected MessageType");
            }
        }

        protected void processIncomingMessage(String message){
            ConsoleHelper.writeMessage (message);
        }

        protected void processIncomingFile(File file){
            ConsoleHelper.writeMessage ("Прислали файл");
        }

        protected void informAboutAddingNewUser(String userName){
            ConsoleHelper.writeMessage ("Участник с именем " + userName+ " присоединился к чату");
        }

        protected void informAboutDeletingNewUser(String userName) {
            ConsoleHelper.writeMessage("Участник с именем " + userName + " покинул чат");
        }

        protected void notifyConnectionStatusChanged(boolean clientConnected2){
            clientConnected = clientConnected2;
            synchronized (Client.this) {
                Client.this.notify();
            }
        }

    }   // окончание класса SocketThread

    protected String getServerAddress(){
          ConsoleHelper.writeMessage ("Введите адрес сервера");
          return ConsoleHelper.readString();
    }

    protected int getServerPort(){
        ConsoleHelper.writeMessage ("Введите # порта");
        return ConsoleHelper.readInt();
    }

    protected String getUserName(){
        ConsoleHelper.writeMessage ("Введите имя пользователя");
        return ConsoleHelper.readString();
    }

    protected boolean shouldSendTextFromConsole(){
        return true;
    }

    protected SocketThread getSocketThread(){
        return new SocketThread();
    }

    protected void sendTextMessage(String text){
        try {
            connection.send(new Message(MessageType.TEXT, text));
        } catch (IOException e) {
            clientConnected = false;
            ConsoleHelper.writeMessage ("Ошибка отправки сообщения");
        }
    }

    protected void sendPrivateTextMessage(String text, String toUserName ){
        try {
            connection.send(new Message(MessageType.PRIVATE_TEXT, text, toUserName));
        } catch (IOException e) {
            clientConnected = false;
            ConsoleHelper.writeMessage ("Ошибка отправки сообщения");
        }
    }

    protected void sendPrivateFile(File file, String toUserName ){
        try {
            connection.send(new Message(MessageType.PRIVATE_FILE, file, toUserName));
        } catch (IOException e) {
            clientConnected = false;
            ConsoleHelper.writeMessage ("Ошибка отправки файла");
        }
    }

    public void run(){
        SocketThread tempSocket = getSocketThread();
        tempSocket.setDaemon(true);
        tempSocket.start();
        try {
            synchronized (this) {
                this.wait();
            }
        } catch (InterruptedException e) {
            ConsoleHelper.writeMessage ("Ошибка");
        }

        if (clientConnected)  ConsoleHelper.writeMessage ("Соединение установлено");
        else ConsoleHelper.writeMessage ("Произошла ошибка во время работы клиента.");

        while (clientConnected){
            String txt =  ConsoleHelper.readString();  // вводим сообщение для отправки
            if (txt.equals("exit")) break;
//*****  проверка ненормативной лексики и бан *******//
            if (bannedWords(txt) == true) {
                sendTextMessage("Пользователь забанен за ненормативную лексику");
                break;
            }

            if (shouldSendTextFromConsole()){
                if (txt.matches("(?iu)Личное\\s+(?iu)сообщение\\s+(?iu)для.+")) {
                    String temp = txt.substring(20);
                    try {
                        String[] temp1 = temp.split(" ");
                        String toUserName = temp1[1].trim();

                        String string = temp.substring(toUserName.length()+1).trim();
                        sendPrivateTextMessage(string, toUserName);
//                        ConsoleHelper.writeMessage ("Личное для "+ toUserName +": " + string);
                    } catch (ArrayIndexOutOfBoundsException e){
                        ConsoleHelper.writeMessage ("Ошибка личного сообщения.+" +
                                " Напишите \"Личное сообщение для\" имя_пользователя");
                    }

                } else
                if (txt.matches("(?iu)Личный\\s+(?iu)файл\\s+(?iu)для.+")) {
                    String temp = txt.substring(15);
                    try {
                        String[] temp1 = temp.split(" ");
                        String toUserName = temp1[1].trim();
                        String filePath = temp.substring(toUserName.length()+1).trim();
                    // ******* выбрать нужный файл и присвоить переменной file ***********
                        File file = new File(filePath);
                        sendPrivateFile(file, toUserName);

                        ConsoleHelper.writeMessage ("Личный файл для "+ toUserName +": ... " );
                    } catch (ArrayIndexOutOfBoundsException e){
                        ConsoleHelper.writeMessage ("Ошибка личного файла. + " +
                                "Напишите \"Личный файл для\" имя_пользователя Файл_с_путем");
                    }

                }
                else
                sendTextMessage(txt);
            }


        }



    }
    protected boolean bannedWords(String str){
        if (str.matches(".*\\b(?iu)убить\\b.*")  ||
                (str.matches(".*\\b(?iu)тебя\\s+(?iu)урою\\b.*")) ||
                (str.matches(".*\\b(?iu)ты\\s+(?iu)гад\\b.*"))   ||
                (str.matches(".*\\b(?iu)ты\\s+(?iu)урод\\b.*")) ||
                (str.matches(".*\\b(?iu)бан\\b.*")))
            return  true;
        else
            return false;
    }



}
