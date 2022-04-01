package classesExec;

import Enum.ComandoEnum;
import java.io.Serializable;



public class Comando implements Serializable {
    //Cria variáveis de outras classes para auxílio no funcionamento
    private ComandoEnum comando;
    private InformacoesCliente informacoesCliente;
    
    //Construtor da classe. Objeto ComandoEnum para verificar o comando
    //que será executado, e InformacoesCliente para pegar as informacoes do cli-
    //ente que será utilizado
    public Comando(ComandoEnum comando, InformacoesCliente informacoesCliente) {
        this.comando = comando;
        this.informacoesCliente = informacoesCliente;
        if (informacoesCliente != null)
            this.informacoesCliente.setOut(null);
    }
    
    // Get Comando
    public ComandoEnum getComando() {
        return comando;
    }
    
    // Get informacoes Cliente
    public InformacoesCliente getInformacoesCliente() {
        return informacoesCliente;
    }
}
