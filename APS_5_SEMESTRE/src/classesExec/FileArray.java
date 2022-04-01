package classesExec;

import Enum.ComandoEnum;
import java.io.Serializable;


public class FileArray extends Comando implements Serializable{
    String[] nomeArquivos;

    public FileArray(ComandoEnum comando, InformacoesCliente informacoesCliente, String[] nomeArquivos) {
        super(comando, informacoesCliente);
        this.nomeArquivos = nomeArquivos;
    }

    public String[] getNomeArquivos() {
        return nomeArquivos;
    }

}
