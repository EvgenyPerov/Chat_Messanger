package myChatMessanger.client;

import myChatMessanger.ClientGuiModel;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;


public class ClientGuiView {
    private final ClientGuiController controller;
    File file;
    private JPanel panel = new JPanel();
    protected JFrame frame = new JFrame("Чат");
    private JTextField textField = new JTextField(50);
    protected JTextArea messages = new JTextArea(10, 50);
    private JTextArea users = new JTextArea(10, 10);
 //   private JCheckBox checkPrivate = new JCheckBox("Личное сообщение");
    protected JLabel label = new JLabel();
    private JComboBox userComboBox = new JComboBox();
    private final JFileChooser fileChooser = new JFileChooser();
    private final JFileChooser fileSaver = new JFileChooser();
    private JButton Send = new JButton("Отправить");
    private JButton Cancel = new JButton("Отмена");
    protected JButton Save = new JButton("Загрузить входящий файл");

    private JMenuBar menuBar = new JMenuBar();
    private JMenu fileMenu = new JMenu("File");
    private JMenuItem sendPrivateFile = new JMenuItem("Send private file");

    private JMenu typeMessage = new JMenu("Type message");
    private ButtonGroup group = new ButtonGroup();
    JRadioButtonMenuItem radioMenuCommon = new JRadioButtonMenuItem("Common", true);
    JRadioButtonMenuItem radioMenuPrivate = new JRadioButtonMenuItem("Private");

    private JMenu botFileMenu = new JMenu("Bot request");
    private JMenuItem botDate = new JMenuItem("Дата");
    private JMenuItem botTime = new JMenuItem("Время");

    public ClientGuiView(ClientGuiController controller) {
        this.controller = controller;
        initView();
    }

    private void initView() {
        textField.setEditable(false);
        messages.setEditable(false);
        messages.setForeground(Color.RED);
        messages.setFont(new Font("Arial!",Font.ITALIC,16));
        users.setEditable(false);
        users.setBackground(Color.YELLOW);
        userComboBox.setEnabled(false);
        Send.setVisible(false);
        Cancel.setVisible(false);
        Save.setVisible(false);

        userComboBox.setSize(users.getColumns(), 1);
        userComboBox.setModel(new DefaultComboBoxModel(controller.getModel().getAllUsersArray()));

//        panel.add(checkPrivate, BorderLayout.EAST);
        panel.add(label, BorderLayout.WEST);
        panel.add(userComboBox, BorderLayout.EAST);
        panel.add(Send, BorderLayout.WEST);
        panel.add(Cancel, BorderLayout.WEST);
        panel.add(Save, BorderLayout.WEST);

        menuBar.add(fileMenu);

        fileMenu.add(sendPrivateFile);
        fileMenu.add(typeMessage);
        fileMenu.add(botFileMenu);

        typeMessage.add(radioMenuCommon);
        typeMessage.add(radioMenuPrivate);
        botFileMenu.add(botDate);
        botFileMenu.add(botTime);

        frame.getContentPane().add(textField, BorderLayout.NORTH);
        frame.getContentPane().add(new JScrollPane(messages), BorderLayout.WEST);
        frame.getContentPane().add(new JScrollPane(users), BorderLayout.EAST);
        frame.getContentPane().add(panel, BorderLayout.SOUTH);
//        UIManager.put("MenuBar.background", Color.ORANGE);
        fileMenu.setMnemonic(KeyEvent.VK_F);
        group.add(radioMenuCommon);
        group.add(radioMenuPrivate);
        frame.setJMenuBar(menuBar);

        frame.pack();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);

        Save.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                fileSaver.setDialogTitle("Сохранение файла");
                fileSaver.setFileSelectionMode(JFileChooser.FILES_ONLY);
                fileSaver.setSelectedFile(controller.getModel().getFile());
                int result = fileSaver.showSaveDialog(frame);
                String fileName;
                if (result == JFileChooser.APPROVE_OPTION) {
                    fileName = fileSaver.getSelectedFile().getPath();  // выбрали путь в диалоговом окне
                    try {
  //                      controller.bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(fileName)));
                        controller.inputStream =  new FileInputStream(controller.getModel().getFile().getPath());
                        controller.outputStream = new FileOutputStream(fileName);

 //                       controller.reader = new BufferedReader(new InputStreamReader(new FileInputStream(controller.getModel().getFile().getPath())));
                        if (controller.inputStream.available() >0) {
                            //читаем весь файл одним куском
                            byte[] buffer = new byte[controller.inputStream.available()];
                            int count = controller.inputStream.read(buffer);
                            controller.outputStream.write(buffer, 0, count);
                        }
                        controller.inputStream.close();
                        controller.outputStream.close();
                    //********** это окно можно убрать *********
                        JOptionPane.showMessageDialog(frame,
                                "Файл '" + fileSaver.getSelectedFile() +
                                        "' сохранен");

                    } catch (FileNotFoundException  fileNotFoundException) {
                        JOptionPane.showMessageDialog(
                                frame,
                                "Файл не найден",
                                "Ошибка сохранения файла",
                                JOptionPane.ERROR_MESSAGE);
                    } catch (IOException ioException) {
                        JOptionPane.showMessageDialog(
                                frame,
                                "Ошибка ввода",
                                "Ошибка сохранения файла",
                                JOptionPane.ERROR_MESSAGE);
                    }
                    Save.setVisible(false);
                    result = 0;
                    fileName = null;
                    controller.getModel().setFile(null);
                    Save.setEnabled(false);
                }
                if (result == JFileChooser.CANCEL_OPTION) {
                    result = 0;
                    fileName = null;
                    controller.getModel().setFile(null);
                    Save.setVisible(false);
                    Save.setEnabled(false);
                }

            }
        });

        sendPrivateFile.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (sendPrivateFile.isArmed()) {
                    // должно появиться окно с выбором файла
                    int result = fileChooser.showOpenDialog(frame);
                    if (result == JFileChooser.APPROVE_OPTION) {
                        file = fileChooser.getSelectedFile();
                        textField.setText(file.getPath());
                        label.setText("Файл выбран. Далее выберите получателя: ");
                        activateComboBox();
                        Send.setVisible(true);
                        Cancel.setVisible(true);
                    }
                }
            }
        });

        Send.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                try {
                    String toUserName = (String) userComboBox.getSelectedItem();
                    if (toUserName != "") {
                        controller.sendPrivateFile(file, toUserName);
                        textField.setText("");
                        userComboBox.setEnabled(false);
                        Send.setVisible(false);
                        Cancel.setVisible(false);
 //                       label.setText("Отправлено" + file.getPath());
                    } else
                        label.setText("Файл выбран. Далее выберите получателя: ");

                } catch (Exception r) {
                    textField.setText("");
                    JOptionPane.showMessageDialog(
                            frame,
                            "Что то пошло не так",
                            "Ошибка отправки файла",
                            JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        Cancel.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                userComboBox.setEnabled(false);
                Send.setVisible(false);
                Cancel.setVisible(false);
                label.setText("");
                textField.setText("");
            }
        });

        radioMenuPrivate.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                activateComboBox();
            }
        });

        radioMenuCommon.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                userComboBox.setEnabled(false);
            }
        });

        botDate.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                controller.sendTextMessage("дата");
            }
        });

        botTime.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                controller.sendTextMessage("время");
            }
        });

        userComboBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
            }
        });

        userComboBox.addMouseListener(new MouseListenerComboBox());  // обработка событий мышки на всплывающем списке

        users.addMouseListener(controller.getPopClickListener());  // обработка правой кнопкой на пользователя - вызов меню

        textField.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (radioMenuCommon.isSelected()) {
                    controller.sendTextMessage(textField.getText());
                    controller.MyMessageWasSend = textField.getText();
                    controller.isPrivate = false;
                    textField.setText("");

                } else
                if (radioMenuPrivate.isSelected()) {
                    String toUserName = (String)userComboBox.getSelectedItem();
                    controller.sendPrivateTextMessage(textField.getText(), toUserName);
                    controller.getSocketThread().processIncomingMessage("Лично для "+ toUserName +": " + textField.getText());
                    controller.MyMessageWasSend = textField.getText();
                    controller.isPrivate = true;
                    textField.setText("");
                }
            }
        });

    }  // конец блока инициализации

    public void activateComboBox() {
        userComboBox.setSize(users.getColumns(),1);
        userComboBox.setEnabled(true);
        userComboBox.setModel(new DefaultComboBoxModel(controller.getModel().getAllUsersArray()));
    }

     public String getServerAddress() {
        return JOptionPane.showInputDialog(
                frame,
                "Введите адрес сервера:",
                "Конфигурация клиента",
                JOptionPane.QUESTION_MESSAGE);
    }

    public int getServerPort() {
        while (true) {
            String port = JOptionPane.showInputDialog(
                    frame,
                    "Введите порт сервера:",
                    "Конфигурация клиента",
                    JOptionPane.QUESTION_MESSAGE);
            try {
                return Integer.parseInt(port.trim());
            } catch (Exception e) {
                frame.setVisible(false);
                JOptionPane.showMessageDialog(
                        frame,
                        "Был введен некорректный порт сервера. Попробуйте еще раз.",
                        "Конфигурация клиента",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    public String getUserName() {
        return JOptionPane.showInputDialog(
                frame,
                "Введите ваше имя:",
                "Конфигурация клиента",
                JOptionPane.QUESTION_MESSAGE);
    }

    public void notifyConnectionStatusChanged(boolean clientConnected) {
        textField.setEditable(clientConnected);
        if (clientConnected) {
            JOptionPane.showMessageDialog(
                    frame,
                    "Соединение с сервером установлено",
                    "Чат",
                    JOptionPane.INFORMATION_MESSAGE);
        } else {
            frame.setVisible(false);
            JOptionPane.showMessageDialog(
                    frame,
                    "Ошибка подключения к серверу. Проверьте адрес и/или порт",
                    "Чат",
                    JOptionPane.ERROR_MESSAGE);
        }

    }

    public void refreshMessages() {
        messages.append((controller.getModel().getNewMessage() + "\n"));
    }

    public void refreshUsers() {
        ClientGuiModel model = controller.getModel();
        StringBuilder sb = new StringBuilder();
        for (String userName : model.getAllUserNames()) {
            sb.append(userName).append("\n");
        }
        users.setText(sb.toString());
    }


    public class MouseListenerComboBox implements MouseListener {

        public void mouseClicked(MouseEvent e) {
            // обработка
        }

        public void mouseEntered(MouseEvent e) {
            // обработка
        }

        public void mouseExited(MouseEvent e) {
            // обработка
        }

        public void mousePressed(MouseEvent e) {
            // обработка
        }

        public void mouseReleased(MouseEvent e) {
            // обработка
        }
    }



}
