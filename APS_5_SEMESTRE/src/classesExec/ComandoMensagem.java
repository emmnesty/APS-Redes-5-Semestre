package classesExec;

import Enum.ComandoEnum;



//Herda de "Comando"
public class ComandoMensagem extends Comando {
    // Variáveis utilizadas na classe
    private int mensagemDe;
    private int mensagemPara;
    private String mensagem;
    
    //Construtor da classe. Utiliza parte do contrutor da classe pai com a parte do comando
    //e da informacoescliente. E ele irá trabalhar com IDs na parte de "mensagemDe" e "mensagemPara", e a
    //string "mensagem" é a mensagem em si.
    public ComandoMensagem(ComandoEnum comando, InformacoesCliente informacoesCliente, int mensagemDe, int mensagemPara, String mensagem) {
        super(comando, informacoesCliente);
        this.mensagemDe = mensagemDe;
        this.mensagemPara = mensagemPara;
        this.mensagem = mensagem;
    }
    
    //Conjuto de métodos Get e Set da classe
    public int getMensagemDe() {
        return mensagemDe;
    }
    
    public int getMensagemPara() {
        return mensagemPara;
    }
    
    public String getMensagem() {
        return mensagem;
    }
    
    public void setMensagem(String mensagem) {
        this.mensagem = mensagem;
    }
}
