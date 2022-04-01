package classesExec;

import Enum.ComandoEnum;
import java.io.Serializable;


public class FileDownload extends Comando implements Serializable {
    private byte[] fileBytes;

    //M�todo Construtor da classe 
    public FileDownload(ComandoEnum comando, InformacoesCliente informacoesCliente, byte[] fileBytes) {
        super(comando, informacoesCliente);
        this.fileBytes = fileBytes;
    }
    
    //M�todos Set e Get
    public void setFileBytes(byte[] fileBytes) {
        this.fileBytes = fileBytes;
    }
    
    public byte[] getFileBytes(){
        return fileBytes;
    }
}
