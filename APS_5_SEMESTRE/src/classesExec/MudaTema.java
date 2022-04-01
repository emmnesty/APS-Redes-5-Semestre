package classesExec;

import Telas.Arquivos;
import Telas.Cadastra_Cliente;
import Telas.Cadastra_Host;
import Telas.ClienteScreen;
import Telas.Emoji;
import Telas.Info;
import Telas.ServidorScreen;
import Telas.inicial;
import java.awt.Color;
import java.io.IOException;
import java.net.Socket;
import javax.swing.JOptionPane;



public abstract class MudaTema extends javax.swing.JFrame {
    
    //Declaração de atributos da classe
    protected static boolean i = true;
    protected static boolean clicou = false;
    
    //Todos os frames
    protected static inicial ini = null;
    protected static Info info = null;
    protected static Cadastra_Host cadHost = null;
    protected static Cadastra_Cliente cadCli = null;
    protected static ServidorScreen serv = null;
    protected static Arquivos arqui = null;
    protected static Emoji emoji = null;
    protected static ClienteScreen cliente = null;
    
    //Cores presentes em todos os frames
    protected Color escuro = new Color(2, 0, 94);
    protected Color claro = new Color(250, 204, 102);
    protected Color botao_claro = new Color(31, 135, 235);
    protected Color botao_escuro = new Color(255, 164, 0);
    protected Color text_escuro = new Color(0, 0, 0);
    protected Color text_claro = new Color(255, 255, 255);
    protected Color area_escuro = new Color(67, 68, 87);
    protected Color area_claro = new Color(220, 220, 220);
    
    
    //Método para ser sobrescrito nas outras classes. Existe para pertencer ans outras
    protected void mudaCor(){}
    
    //Alterar o valor do "clicou", alterando a cor
    public void setCor(){
        clicou = !clicou;
    }
    
    //Pega valor do "clicou"
    public boolean getCor(){
        return clicou;
    }
    
    //Cria o frame caso ele não exista
    public void criaInfo(){
        if(info == null){
            info = new Info();
        }
    }
    
    public void criaCadHost(){
        if(cadHost == null){
            cadHost = new Cadastra_Host();
        }
    }
    
    public void criaCadCli(){
        if(cadCli == null){
            cadCli = new Cadastra_Cliente();
        }
    }
    
    public void criaServ(){
        if(serv == null){
            serv = new ServidorScreen();
            i = false;
        }
    }
    
    public void criaCliente(InformacoesCliente infoCliente, Socket socket){
        try {
        cliente = new ClienteScreen(infoCliente, socket);
        } catch (IOException error) {
            JOptionPane.showMessageDialog(null, "Servidor não encontrado, verifique se os dados estão corretos.", "Servidor não encontrado", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    public void criaArquivo(int id, ClienteSocket clienteSocket, ClienteScreen cliente){
        if(arqui == null){
            arqui = new Arquivos(id, clienteSocket, cliente);
        }
    }
    
    public void criaEmoji(ClienteScreen cliente){
        if(emoji == null){
            emoji = new Emoji(cliente);
        }
    }
    
    public void setIni(inicial ino){
        ini = ino;
    }
}
