package classesExec;


public abstract class Informacoes {
    //Variáveis utilizadas na classe
    private String ipPublicoServidor;
    private String ipLocalServidor;
    //Portas do computador utilizadas para comunicacao
    private final int portServidor = 57311;
    private final int portFileUpload = 57312;
    private final int portFileDownload = 57313;
    private final int portArrayFiles = 57314;
    private final String diretorioArquivos = "./tempServer/";
    
    //Conjuntos de métodos Get e Set da classe
    public String getIpPublicoServidor() {
        return ipPublicoServidor;
    }
    
    public void setIpPublicoServidor(String ipPublicoServidor) {
        this.ipPublicoServidor = ipPublicoServidor;
    }
    
    public String getIpLocalServidor() {
        return ipLocalServidor;
    }
    
    public void setIpLocalServidor(String ipLocalServidor) {
        this.ipLocalServidor = ipLocalServidor;
    }
    
    public int getPortServidor() {
        return portServidor;
    }
    
    public int getPortFileUpload() {
        return portFileUpload;
    }
    
    public int getPortFileDownload() {
        return portFileDownload;
    }
    
    public int getPortArrayFiles(){
        return portArrayFiles;
    }
    
    public String getDiretorioArquivos() {
        return diretorioArquivos;
    }
}
