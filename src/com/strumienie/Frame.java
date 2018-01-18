package com.strumienie;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;

public class Frame extends JFrame
{
    File selectedFile = null;
    JEditorPane ep = new JEditorPane();
    JPanel panelSouth = new JPanel();
    JPanel panelNorth = new JPanel();
    JLabel fileInfo = new JLabel();
    JButton fileButton = new JButton("Choose new file");
    JButton saveButton = new JButton("Clear & save");
    String selections[] = {"UTF_8", "ISO_8859_1", "US_ASCII"};
    JComboBox encoding = new JComboBox(selections);

    public Frame() throws IOException
    {
        try
        {
            File f = new File("text1.txt");
            selectedFile = f;
            ep.setEditable(true);
            ep.setPage(f.toURI().toURL());
            ep.setFont(new Font("Segoe Script",0,32));
            updateAttributes(selectedFile);

            encoding.setSelectedIndex(0);

            fileButton.addActionListener(actListFile);
            saveButton.addActionListener(actListSave);


            setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            add(ep,BorderLayout.CENTER);
            panelSouth.add(encoding,BorderLayout.WEST);
            panelSouth.add(fileButton,BorderLayout.CENTER);
            panelSouth.add(saveButton,BorderLayout.EAST);
            panelNorth.add(fileInfo,BorderLayout.CENTER);
            add(panelNorth,BorderLayout.NORTH);
            add(panelSouth,BorderLayout.SOUTH);
            setSize(1920,1080);
            setVisible(true);
        }
        catch(IOException e)
        {
            e.printStackTrace();
        }


    }
    public File chooseFile()
    {
        JFileChooser fileChooser = new JFileChooser();
        if(fileChooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION)
        {
            try
            {
                selectedFile = fileChooser.getSelectedFile();
                updateAttributes(selectedFile);
            }
            catch (Exception ex)
            {
                ex.printStackTrace();
            }
        }
        return selectedFile;
    }

    public void updateEditorPane(File f) throws IOException
    {
        try
        {
            String str = "";
            String line;
            BufferedReader in = new BufferedReader(new FileReader(selectedFile));
            while((line = in.readLine()) != null)
                str += line + "\n";
            ep.setText(str);
        }
        catch(IOException ex)
        {
            ex.printStackTrace();
        }
    }
    private ActionListener actListFile = new ActionListener()
    {
        public void actionPerformed(ActionEvent e)
        {
            try
            {
                File f = chooseFile();
                updateEditorPane(f);
            }
            catch(IOException ex)
            {
                ex.printStackTrace();
            }

        }
    };

    private ActionListener actListSave = new ActionListener()
    {
        public void actionPerformed(ActionEvent e)
        {
            String str = ep.getText();

            try
            {
                BufferedWriter out;
                switch(encoding.getSelectedIndex())
                {
                    case 0:
                        out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(selectedFile), StandardCharsets.UTF_8));
                        break;
                    case 1:
                        out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(selectedFile), StandardCharsets.ISO_8859_1));
                        break;
                    case 2:
                        out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(selectedFile), StandardCharsets.US_ASCII));
                        break;
                    default:
                        out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(selectedFile), StandardCharsets.UTF_8));
                        break;
                }
                out.write(str);
                out.close();
                clearStream(selectedFile);
                copyFromTmp(selectedFile);
                updateEditorPane(selectedFile);
                updateAttributes(selectedFile);
            }
            catch(IOException ex)
            {
                ex.printStackTrace();
            }

        }

    };

    public static void clearStream(File f) throws IOException
    {
        FileReader inputStream = new FileReader(f);
        FileWriter outputStream = new FileWriter("tmp");

        List<Integer> charsInFile = new ArrayList<Integer>();

        try
        {
            int c = inputStream.read();
            boolean firstLine = true; //jesli prawda tzn ze operujemy na pierwszej linii w pliku

            while (c != -1)
            {
                if(firstLine == true || charsInFile.get(charsInFile.size() - 1) == 10)
                {
                    while(c == 32 || c == 9)
                    {
                        if(c == 9)
                        {
                            for(int i = 0; i < 7; i++)
                                charsInFile.add(32);
                        }
                        else
                            charsInFile.add(c);
                        c = inputStream.read();
                        firstLine = false;
                    }
                }
                while(c == 32 || c == 9)
                {
                    c = inputStream.read();
                    if(c != 32 && c != 9 && c != 10)
                    {
                        charsInFile.add(32);
                    }
                }

                charsInFile.add(c);
                firstLine = false;
            //    if(c == 10 && (charsInFile.get(charsInFile.size() - 2) == 32 || charsInFile.get(charsInFile.size() - 2) == 9))
             //       charsInFile.remove(charsInFile.get(charsInFile.size() - 2));
                c = inputStream.read();


            }
            for(int i = 0; i < charsInFile.size(); i++)
            {
                outputStream.write(charsInFile.get(i));
            }
        }
        finally
        {
            if(inputStream != null)
                inputStream.close();
            if(outputStream != null)
                outputStream.close();
        }
    }

    public static void copyFromTmp(File f) throws IOException
    {
        FileReader inputStream = new FileReader("tmp");
        FileWriter outputStream = new FileWriter(f);

        try
        {
            int c = inputStream.read();
            while(c != -1)
            {
                outputStream.write(c);
                c = inputStream.read();
            }
        }
        finally
        {
            if (inputStream != null)
                inputStream.close();
            if(outputStream != null)
                outputStream.close();
        }
    }

    public void updateAttributes(File f) throws IOException
    {
        Path file = Paths.get(f.toURI());
        BasicFileAttributes attr = Files.readAttributes(file,BasicFileAttributes.class);
        fileInfo.setText("Filename: " + f.getName() + ", creation time: " + attr.creationTime() + ", last modified: " + attr.lastModifiedTime() + ", size: " + attr.size() + "B, path: " + file);
    }
}


