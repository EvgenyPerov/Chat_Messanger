package myChatMessanger.client;

import myChatMessanger.ClientGuiModel;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.*;

public class ClientGuiController extends Client {
    private ClientGuiModel model = new ClientGuiModel();
    private ClientGuiView view = new ClientGuiView(this);
    protected FileInputStream inputStream;
    protected FileOutputStream outputStream;
    protected String MyMessageWasSend;
    protected boolean isPrivate;
    public String user;

    public static void main(String[] args) {
        new ClientGuiController().run();
    }

    public class GuiSocketThread extends SocketThread {

       public void processIncomingMessage(String message){
           model.setNewMessage(message);
           view.refreshMessages();

           String inputMessage = message.substring(message.indexOf(":")+2);
           user = message.substring(0,message.indexOf(":"));

           // *****настройка цвета фона ***
           if (inputMessage.equals(MyMessageWasSend))
               view.messages.setBackground(Color.GRAY);
           else
               view.messages.setBackground(Color.BLUE);

           if (bannedWords(inputMessage) == true) {
               sendTextMessage("Пользователь "+user+" забанен за ненормативную лексику");
               try{
                   view.frame.dispose();
               clientConnected = false;
               connection.close();
               getSocketThread().interrupt();
               Thread.currentThread().interrupt();
           } catch (IOException e) {
               e.printStackTrace();
           }

           JOptionPane.showMessageDialog(
                   view.frame,
                   "Бан за ненормативную лексику. Соединение закрыто",
                   "Блокировка клиента",
                   JOptionPane.ERROR_MESSAGE);
           }

       }

        protected void processIncomingFile(File file){
            model.setFile(file);
            view.Save.setVisible(true);
            view.Save.setEnabled(true);

        }

        public void informAboutAddingNewUser(String userName){
           model.addUser(userName);
           view.refreshUsers();
       }

       public void informAboutDeletingNewUser(String userName){
           model.deleteUser(userName);
           view.refreshUsers();
       }

       public void notifyConnectionStatusChanged(boolean clientConnected){
           view.notifyConnectionStatusChanged(clientConnected);
       }
    }

    protected SocketThread getSocketThread() {
        return new GuiSocketThread();
    }

    @Override
    public void run() {
        getSocketThread().run();
    }

    @Override
    public int getServerPort() {
        return view.getServerPort();
    }

    @Override
    public String getUserName() {
        return view.getUserName();
    }

    @Override
    public String getServerAddress() {
        String adress = view.getServerAddress();
        while (adress.isEmpty()) {
            adress = view.getServerAddress();
        }
        return adress;
    }

    public ClientGuiModel getModel(){
        return model;
    }

    private class PopUpDemo extends JPopupMenu {
        JMenuItem botItems;
        public PopUpDemo(){
            botItems = new JMenuItem("Bot request...");
            add(botItems);

            add(new JMenuItem("время",7),botItems,0);
            add(new JMenuItem("дата"),botItems,1);

            add(new JMenuItem("Smiles..."));
            add(new JMenuItem("Send file"));
        }
    }

    protected class PopClickListener extends MouseAdapter {
        /*       public void mousePressed(MouseEvent e){
                   if (e.isPopupTrigger())
                       doPop(e);
               }
       */        public void mouseReleased(MouseEvent e){
            if (e.isPopupTrigger()) {
                PopUpDemo menu = new PopUpDemo();
                menu.show(e.getComponent(), e.getX(), e.getY());
            }
        }
    }

    protected PopClickListener getPopClickListener() {
        return new PopClickListener();
    }

}
