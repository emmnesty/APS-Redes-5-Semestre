package classesExec;

import Enum.ComandoEnum;



//Herda de "Comando"
public class ComandoMensagem extends Comando {
    // Vari�veis utilizadas na classe
    private int mensagemDe;
    private int mensagemPara;
    private String mensagem;
    
    //Construtor da classe. Utiliza parte do contrutor da classe pai com a parte do comando
    //e da informacoescliente. E ele ir� trabalhar com IDs na parte de "mensagemDe" e "mensagemPara", e a
    //string "mensagem" � a mensagem em si.
    public ComandoMensagem(ComandoEnum comando, InformacoesCliente informacoesCliente, int mensagemDe, int mensagemPara, String mensagem) {
        super(comando, informacoesCliente);
        this.mensagemDe = mensagemDe;
        this.mensagemPara = mensagemPara;
        this.mensagem = mensagem;
    }
    
    //Conjuto de m�todos Get e Set da classe
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
