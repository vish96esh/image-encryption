import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.awt.image.*;
import javax.imageio.ImageIO;
import java.io.*;
 
public class Server
{
 
    private static Socket socket;
    static int plain_text[]=new int[18000000] ;
    static int encrypt_text[]=new int[18000000] ;
    static int width;
    static int height;
    static int total_pix;
    static int key=5;
    

    public static void printAllRGBDetails(BufferedImage image) throws FileNotFoundException,Exception
    {
        width = image.getWidth();
        height = image.getHeight();
        int pix_num=1;
        total_pix=width*height;
        String RGBvalue="";
        int index=0;

        for (int i = 0; i < width; i++)
        {
            for (int j = 0; j < height; j++)
            {
                int pixel = image.getRGB(i, j);
                
                int red = (pixel >> 16) & 0x000000FF;
                plain_text[index++]=red;
                int green = (pixel >>8 ) & 0x000000FF;
                plain_text[index++]=green;
                int blue = (pixel) & 0x000000FF;
                plain_text[index++]=blue;
                RGBvalue =  red + " " + green + " " + blue;
                System.out.print(RGBvalue);
                
                if(pix_num<total_pix)       //To delete the line that generates at end of file
                {
                    System.out.println("");
                }
                pix_num++;
            }
        }
    }

    public static void encrypt() throws FileNotFoundException, IOException
    {
        int n1=0,n2=1,n3,i;    
        int sum=n2;
    
        for(i=1; i < key; ++i)    
        {    
            n3 = n1 + n2;    
            sum = sum + n3;    
            n1 = n2;    
            n2 = n3;    
        } 

        int replaceVal, component; 
        for (int j = 0; j < 3*total_pix; j++)
        {
            if(j%2 != 0)
            {
                component = key + plain_text[j];
                if(component > 255)
                    replaceVal = (component % 255) - 1;
                else 
                    replaceVal = component; 
                
                encrypt_text[j] = replaceVal;
            }
            else
            {
                component = plain_text[j] - key;
                if(component < 0)
                    replaceVal = 256 + component;
                else 
                    replaceVal = component; 
                
                encrypt_text[j] = replaceVal;
            }
        }


        write_encrypted_array();
    }

    public static BufferedImage readImage(String fileLocation) throws IOException
    {
        BufferedImage img = null;
        img = ImageIO.read(new File(fileLocation));
        return img;
    }

    public static void write_encrypted_array() throws FileNotFoundException, IOException
    {
        File output_file=new File("C:/Users/Pankaj/Desktop/cn/encrypt_pixel.txt");                   
        PrintStream out=new PrintStream(new FileOutputStream(output_file));
        System.setOut(out);

        for(int i=0;i<3*total_pix;i+=3)
        {
            System.out.print(encrypt_text[i]+" "+encrypt_text[i+1]+" "+encrypt_text[i+2]+" ");
            System.out.println();
        }


    }
    public static void create_image() throws FileNotFoundException, IOException
    {
        BufferedImage image = new BufferedImage(height, width, BufferedImage.TYPE_INT_RGB); 

        int x,y,index=0;
        for (x = 0; x < width; x++) 
        {
            for(y = 0; y < height; y++) 
            {
                int rgb = encrypt_text[index++];
                rgb = (rgb << 8) + encrypt_text[index++]; 
                rgb = (rgb << 8) + encrypt_text[index++];
                image.setRGB(y, x, rgb);
            }   
        }

        File outputFile = new File("C:/Users/Pankaj/Desktop/cn/output2.jpg");
        ImageIO.write(image, "jpg", outputFile);
    }



    public static void main(String[] args) throws FileNotFoundException, IOException,Exception
    {
        
        try
        {
            System.out.println("Encryption begin.. wait for few seconds");
            File output_file=new File("C:/Users/Pankaj/Desktop/cn/pixel.txt");                   //Output Text file
            PrintStream stdout=System.out;
            PrintStream out=new PrintStream(new FileOutputStream(output_file));
            System.setOut(out);
            BufferedImage image1 = readImage("C:/Users/Pankaj/Desktop/cn/rainbow.jpg");               //Input Image file
            printAllRGBDetails(image1);
            encrypt();
            create_image();
            
            System.setOut(stdout);
            int port = 25000;
            ServerSocket serverSocket = new ServerSocket(port);
            System.out.println("Encryption Done");
            System.out.println("Server Started and listening to the port 25000");
 
            //Server is running always. This is done using this while(true) loop
            while(true)
            {
                //Reading the message from the client
                socket = serverSocket.accept();
                InputStream is = socket.getInputStream();
                InputStreamReader isr = new InputStreamReader(is);
                BufferedReader br = new BufferedReader(isr);
                String number = br.readLine();
                System.out.println("Request received from client ");
 
                
                
                OutputStream os = socket.getOutputStream();
                /*ObjectOutputStream oss = new ObjectOutputStream(os);
                 
                    oss.writeObject(encrypt_text);
                    oss.flush();
                 
                System.out.println("Encrypted Data sent to the client");
                
                
                os = socket.getOutputStream();*/
                OutputStreamWriter osw = new OutputStreamWriter(os);
                BufferedWriter bw = new BufferedWriter(osw);
                bw.write(String.valueOf(width)+"\n");
                bw.flush();
                bw.write(String.valueOf(height)+"\n");
                bw.flush();
                bw.write(String.valueOf(total_pix)+"\n");
                bw.flush();
                for(int i=0;i<3*total_pix;i++)
                {   
                    bw.write(String.valueOf(encrypt_text[i])+"\n");
                    bw.flush();
                    if(i%1000000==0) System.out.println(i+" points sent.");
                }

                System.out.println("Entire data sent to the client");
                    

            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        finally
        {
            try
            {
                socket.close();
            }
            catch(Exception e){}
        }
    }
}