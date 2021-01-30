package myChatMessanger.client;

import myChatMessanger.ConsoleHelper;
import myChatMessanger.Message;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;

public class BotClient extends Client{
    protected Date dateStart = new Date();

    public static void main(String[] args){
        BotClient botClient = new BotClient();
        botClient.run();

    }

    public class BotSocketThread extends SocketThread{
        Message message;
        Date dateNow;
        @Override
        protected void clientMainLoop() throws IOException, ClassNotFoundException {
            sendTextMessage("Привет чатику. Я бот. Понимаю команды: дата, день, месяц, год, время, час, минуты, секунды. \n" +
                    "Задать вопрос можно в формате: \"Вопрос для бота - .........\"");
            super.clientMainLoop();
        }

        @Override
        protected void processIncomingMessage(String message) {
            dateNow = new Date();
//            ConsoleHelper.writeMessage ("dateStart = " + dateStart.getTime() +
 //                   " dateNow = " + dateNow.getTime()+ " прошло "+ (dateNow.getTime() - dateStart.getTime()));
            if (!message.contains(": ") || message == null) return;
            super.processIncomingMessage(message);
            String[] array = message.split(":");
            String name = array[0].trim();
            String txt = array[1].trim();
            if (txt.matches("^(?iu)Вопрос\\s+(?iu)для\\s+(?iu)бота\\s?-.*")) {
                String[] array2 = message.split("-");
                String question = array2[1];
                if (question != "") {
                    ConsoleHelper.writeMessage ("Бот начинает процесс обработки вопроса...");
                    sendTextMessage("Ответ на вопрос от "+ name+ ": " + questionBot(question));
                }
            }

            if ((dateNow.getTime() - dateStart.getTime()) >= 120000) {   // каждые 120 секунд выводить сообщение после отправки
                sendTextMessage("Я бот. Понимаю команды: дата, день, месяц, год, время, час, минуты, секунды. \n" +
                        "Также можно задать вопрос в формате: \"Вопрос для бота - .........\"");
                dateStart = new Date();

            }


            Calendar calendar = new GregorianCalendar();
            DateFormat dateFormat = new SimpleDateFormat("d.MM.YYYY", Locale.ENGLISH);
            DateFormat MonthFormat = new SimpleDateFormat("MMMM", Locale.ENGLISH);
            DateFormat timeFormat = new SimpleDateFormat("H:mm:ss", Locale.ENGLISH);

            if (txt != null){
                switch (txt){
                    case "дата" :     sendTextMessage("Информация для "+ name+ ": "
                            + dateFormat.format(calendar.getTime()));
                    break;
                    case "день" : sendTextMessage("Информация для "+ name+ ": "
                        + calendar.get(Calendar.DAY_OF_MONTH));
                    break;
                    case "месяц" : sendTextMessage("Информация для "+ name+ ": "
                        + MonthFormat.format(calendar.getTime()));
                    break;
                    case "год" : sendTextMessage("Информация для "+ name+ ": "
                        + calendar.get(Calendar.YEAR));
                    break;
                    case "время" : sendTextMessage("Информация для "+ name+ ": "
                        + timeFormat.format(calendar.getTime()));
                    break;
                    case "час" : sendTextMessage("Информация для "+ name+ ": "
                        + calendar.get(Calendar.HOUR_OF_DAY));
                    break;
                    case "минуты" : sendTextMessage("Информация для "+ name+ ": "
                        + calendar.get(Calendar.MINUTE));
                    break;
                    case "секунды" : sendTextMessage("Информация для "+ name+ ": "
                        + calendar.get(Calendar.SECOND));
                    break;
                }
            }

        }
    }

    protected SocketThread getSocketThread(){            return new BotSocketThread();         }

    protected boolean shouldSendTextFromConsole(){
        return false;
    }

    protected   String getUserName(){
        return "date_bot_" + (int) (Math.random()*100);
    }

    protected String questionBot(String str){
        String question="не знаю ответ";

        if (str.matches(".*\\b(?iu)дела\\b.*"))                  question ="дела отлично!";
        if (str.matches(".*\\b(?iu)тебя\\s+(?iu)дела\\b.*"))      question ="у меня дела отлично!";
        if (str.matches(".*\\b(?iu)тебя\\s+(?iu)зовут\\b.*"))      question ="мое имя Бот";
        if (str.matches(".*\\b(?iu)ты\\s+(?iu)бот\\b.*"))      question ="я Бот, а не человек";
        if (str.matches(".*\\b(?iu)ты\\s+(?iu)человек\\b.*"))      question ="я Бот, а не человек";
        return question;
    }
}
