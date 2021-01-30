package myChatMessanger;


import java.io.File;
import java.util.*;

public class ClientGuiModel {
    private final Set<String> allUserNames = new  TreeSet<>();

    private String newMessage;

    private File file;

    public Set<String> getAllUserNames() {
        Collections.unmodifiableSet(allUserNames);
        return allUserNames;
    }

    public String[] getAllUsersArray(){
        String[] result = null;
        if (getAllUserNames() != null || !getAllUserNames().isEmpty()) {
            List<String> list = new ArrayList<>(getAllUserNames());
            Collections.sort(list);
            result = new String[list.size()+1];
            result[0]= "";    // пусть первая строка всегда пустая в списке пользователей
            for (int i = 1; i < list.size()+1; i++) {
                result[i] = list.get(i-1);
            }
        }
        return result;
    }

    public void addUser(String newUserName){
        allUserNames.add(newUserName);
    }

    public void deleteUser(String userName){
        allUserNames.remove(userName);
    }

    public String getNewMessage() {
        return newMessage;
    }

    public void setNewMessage(String newMessage) {
        this.newMessage = newMessage;
    }

    public File getFile() {
        return file;
    }

    public void setFile(File file) {
        this.file = file;
    }
}
