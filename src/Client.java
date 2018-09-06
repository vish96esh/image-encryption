import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.awt.image.*;
import javax.imageio.ImageIO;
import java.io.*;
 
public class Client
{
 
    private static Socket socket;
    static int decrypt_text[]=new int[18000000] ;
    static int encrypt_text[]=new int[18000000] ;
    
    static int width;
    static int height;
    static int total_pix;
    static int key=5;
    

    public static void create_image() throws FileNotFoundException, IOException
    {
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB); 

        int x,y,index=0;
        for (x = 0; x < width; x++) 
        {
            for(y = 0; y < height; y++) 
            {
                int rgb = decrypt_text[index++];
                rgb = (rgb << 8) + decrypt_text[index++]; 
                rgb = (rgb << 8) + decrypt_text[index++];
                image.setRGB(x, y, rgb);
            }   
        }

        File outputFile = new File("C:/Users/Pankaj/Desktop/cn/output3.jpg");
        ImageIO.write(image, "jpg", outputFile);
    }
    public static void decrypt() throws FileNotFoundException, IOException
    {
        int n1=0,n2=1,n3;    
        int sum=n2; 
        
        for(int i=1; i < key; ++i)    
        {    
            n3 = n1 + n2;    
            sum = sum + n3;    
            n1 = n2;    
            n2 = n3;    
        }    

        int replaceVal,component; 
        for (int j = 0; j < 3*total_pix; j++)
        {
            if(j% 2 != 0)
            {
                component = encrypt_text[j] - key;
                if(component < 0)
                    replaceVal = 256 + component ;
                else 
                    replaceVal = component; 
                
                decrypt_text[j] = replaceVal;
                
            }
            else
            {
                component = key + encrypt_text[j];
                if(component > 255)
                    replaceVal = (component % 255) - 1;
                else 
                    replaceVal = component; 
                
                decrypt_text [j]= replaceVal;
            }
        }
        write_decrypted_array();
    }

    public static void write_decrypted_array() throws FileNotFoundException, IOException
    {
        File output_file=new File("C:/Users/Pankaj/Desktop/cn/decrypt_pixel.txt");                   
        PrintStream out=new PrintStream(new FileOutputStream(output_file));
        System.setOut(out);

        for(int i=0;i<3*total_pix;i+=3)
        {
            System.out.print(decrypt_text[i]+" "+decrypt_text[i+1]+" "+decrypt_text[i+2]+" ");
            System.out.println();
        }


    }
 
    public static void main(String[] args) throws Exception
    {
         try
        {
            String host = "localhost";
            int port = 25000;
            InetAddress address = InetAddress.getByName(host);
            socket = new Socket(address, port);
 
            //Send the message to the server
            OutputStream os = socket.getOutputStream();
            OutputStreamWriter osw = new OutputStreamWriter(os);
            BufferedWriter bw = new BufferedWriter(osw);
 
            String number = "2";
 
            String sendMessage = number + "\n";
            bw.write(sendMessage);
            bw.flush();
            System.out.println("Request sent to the server");
 
            //Get the return message from the server
            
            InputStream is = socket.getInputStream();
            /*ObjectInputStream iss = new ObjectInputStream(is);
            encrypt_text = (int[])(iss.readObject());
            
            System.out.println("Request acknowledged");

            System.out.println("Recieved encrypted data from server");

            
            is = socket.getInputStream();*/
            InputStreamReader isr = new InputStreamReader(is);
            BufferedReader br = new BufferedReader(isr);
            String message = br.readLine();
            width=Integer.parseInt(message);
            message = br.readLine();
            height=Integer.parseInt(message);
            message = br.readLine();
            total_pix=Integer.parseInt(message);

            for(int i=0;i<3*total_pix;i++)
            {   
                message = br.readLine();
                encrypt_text[i]=Integer.parseInt(message);
                if(i%1000000==0) System.out.println(i+" points recieved.");
            }
            
            
            System.out.println("Recieved entire data from server");
        }
        catch (Exception exception)
        {
            exception.printStackTrace();
        }
        finally
        {
            //Closing the socket
            try
            {
                socket.close();
            }
            catch(Exception e)
            {
                e.printStackTrace();
            }
        }
        System.out.println("Decryption started");
        File output_file=new File("C:/Users/Pankaj/Desktop/cn/pixel2.txt");                   //Output Text file
        PrintStream out=new PrintStream(new FileOutputStream(output_file));
        System.setOut(out);
        
        decrypt();
        System.out.println("Decryption completed. Creating image");
        
        create_image();

        System.out.println("Image created");
        
       
    }
}
