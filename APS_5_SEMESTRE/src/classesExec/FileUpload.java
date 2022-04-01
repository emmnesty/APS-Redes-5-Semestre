package classesExec;

import Enum.ComandoEnum;
import java.io.Serializable;


public class FileUpload extends FileDownload implements Serializable {
    private final String nome;
    private final long currentTimeMillis;
    private int arquivoPara;
    
    //Método Construtor da classe 
    public FileUpload(ComandoEnum comando, InformacoesCliente informacoesCliente, byte[] fileBytes, String nome, int arquivoPara) {
        super(comando, informacoesCliente, fileBytes);
        this.nome = nome;
        this.currentTimeMillis = System.currentTimeMillis();
        this.arquivoPara = arquivoPara;
    }
    
    //Métodos Set e Get
    public String getNome() {
        return nome;
    }
    
    public long getCurrentTimeMillis() {
        return currentTimeMillis;
    }
    
    public int getArquivoPara() {
        return arquivoPara;
    }
}
