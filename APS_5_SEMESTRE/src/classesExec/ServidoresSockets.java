package classesExec;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import Enum.ComandoEnum;


public class ServidoresSockets {
    //Atributos da classe
    private InformacoesServidor informacoesServidor;
    private boolean servidoresContinuemOuvindo;
    //um server socket para cada "operação"
    private ServerSocket mensagensSocket;
    private ServerSocket fileUploadSocket;
    private ServerSocket fileDownloadSocket;
    private ServerSocket arrayFilesSocket;
    //um runable para cada "operação"
    private Runnable runnableMensagemSocket;
    private Runnable runnableFileUploadSocket;
    private Runnable runnableFileDownloadSocket;
    private Runnable runnableArrayFilesSocket;
    //uma thread para cada operação
    private Thread threadMensagemSocket;
    private Thread threadFileUploadSocket;
    private Thread threadFileDownloadSocket;
    private Thread threadArrayFilesSocket;
    public static List<ObjectOutputStream> outEuEOutrosClientes;
    
    //Construtor ad classe
    public ServidoresSockets() {
        //cria uma instância
        this.informacoesServidor = InformacoesServidor.getInstance();
        try {
            //Pega o IP local do servidor
            byte[] ipLocalByteArr = InetAddress.getByName(informacoesServidor.getIpLocalServidor()).getAddress();
            //Cria os sockets nos IPs publico e local
            this.mensagensSocket = new ServerSocket(informacoesServidor.getPortServidor(), 1, InetAddress.getByAddress(informacoesServidor.getIpPublicoServidor(), ipLocalByteArr));
            this.fileUploadSocket = new ServerSocket(informacoesServidor.getPortFileUpload(), 1, InetAddress.getByAddress(informacoesServidor.getIpPublicoServidor(), ipLocalByteArr));
            this.fileDownloadSocket = new ServerSocket(informacoesServidor.getPortFileDownload(), 1, InetAddress.getByAddress(informacoesServidor.getIpPublicoServidor(), ipLocalByteArr));
            this.arrayFilesSocket = new ServerSocket(informacoesServidor.getPortArrayFiles(), 1, InetAddress.getByAddress(informacoesServidor.getIpPublicoServidor(), ipLocalByteArr));
        } catch (IOException error) {
            System.out.println(error.getMessage());
        }
    }
    
    //Seta servidoresContinuemOuvindo como verdade, para que as quatro threads
    //dos sockets fiquem rodando em loop dentro de um while que continua se executando
    //enquanto servidoresContinuemOuvindo for verdade. Ou seja, inicializa as quatro threads
    public void servidoresComecemAOuvir() {
        servidoresContinuemOuvindo = true;
        mensagemSocketComeceAOuvir();
        fileUploadSocketComeceAOuvir();
        fileDownloadSocketComeceAOuvir();
        arrayFilesSocketComeceAOuvir();
    }
    
    //Cria o runnable de mensagens
    private void mensagemSocketComeceAOuvir() {
        this.runnableMensagemSocket = () -> {
            try {
                outEuEOutrosClientes = new ArrayList<>();
                while(servidoresContinuemOuvindo) {
                    //Aceita cliente
                    Socket socketClient = this.mensagensSocket.accept();
                    ObjectInputStream in = new ObjectInputStream(socketClient.getInputStream());
                    Comando comandoObj = (Comando) in.readObject();
                    ObjectOutputStream out = new ObjectOutputStream(socketClient.getOutputStream());
                    //Registra cliente no servidor
                    InformacoesCliente informacoesEuCliente = this.registrarCliente(comandoObj);
                    informacoesEuCliente.setOut(out);
                    //Print na nova conexão de cliente e nos IPs
                    System.out.println("New connection from: " + informacoesEuCliente.getIpPublicoCliente() + "/" + informacoesEuCliente.getIpLocalCliente());
                    //Adiciona o cliente
                    Iterator<ObjectOutputStream> iterador = outEuEOutrosClientes.iterator();
                    for (int i = 0; iterador.hasNext(); i++) {
                        ObjectOutputStream outOutroCliente = iterador.next();
                        outOutroCliente.writeObject(new Comando(ComandoEnum.ADICIONAR, informacoesEuCliente));
                        outOutroCliente.flush();

                        InformacoesCliente esteOutroCliente = this.informacoesServidor.getCliente(i);
                        if (esteOutroCliente != null) {
                            out.writeObject(new Comando(ComandoEnum.ADICIONAR, esteOutroCliente));
                            out.flush();
                        }
                    }
                    outEuEOutrosClientes.add(out);
                    out.writeObject(new Comando(ComandoEnum.REGISTRAR, informacoesEuCliente));
                    out.flush();
                    
                    List<ComandoMensagem> todasAsMensagens = informacoesServidor.getTodasAsMensagensPara(0);
                    for (int i = 0, l = todasAsMensagens.size(); i < l; i++) {
                        ComandoMensagem mensagemObj = todasAsMensagens.get(i);
                        out.writeObject(mensagemObj);
                        out.flush();
                    }
                    new ThreadMensagens(socketClient, informacoesEuCliente, this, in, out).start();
                }
            } catch (IOException | ClassNotFoundException error) {
                System.out.println(error.getMessage());
            }
        };
        this.threadMensagemSocket = new Thread(runnableMensagemSocket);
        this.threadMensagemSocket.start();
    }
    
    //Cria o runnable de fileupload
    private void fileUploadSocketComeceAOuvir() {
        this.runnableFileUploadSocket = () -> {
            while (servidoresContinuemOuvindo) {
                Socket client = null;
                //Aceita o cliente
                try {
                    client = this.fileUploadSocket.accept();
                } catch (IOException error) {
                    System.out.println(error.getMessage());
                }
                if (client != null) {
                    try {
                        ObjectInputStream objectInputStream = new ObjectInputStream(client.getInputStream());
                        FileUpload fileUpload = (FileUpload) objectInputStream.readObject();
                        String diretorio = informacoesServidor.getDiretorioArquivos();
                        String nomeArquivo = diretorio + fileUpload.getCurrentTimeMillis() + "_" + fileUpload.getNome();
                        if (!new File(diretorio).exists()) {
                            new File(diretorio).mkdirs();
                        }
                        new File(nomeArquivo).createNewFile();
                        try (OutputStream outputStream = new FileOutputStream(nomeArquivo)) {
                            outputStream.write(fileUpload.getFileBytes());
                        }
                    } catch (IOException | ClassNotFoundException error) {
                        try {
                            System.out.println(error.getMessage());
                            client.getOutputStream().close();
                            client.getInputStream().close();
                            client.close();
                        } catch (IOException ex) {
                            System.out.println(ex.getMessage());
                        }
                    }
                }
            }
        };
        this.threadFileUploadSocket = new Thread(this.runnableFileUploadSocket);
        this.threadFileUploadSocket.start();
    }
    
    //Cria o runnable de download
    private void fileDownloadSocketComeceAOuvir() {
        this.runnableFileDownloadSocket = () -> {
            while (servidoresContinuemOuvindo) {
                Socket client = null;
                //Aceita cliente
                try {
                    client = this.fileDownloadSocket.accept();
                } catch (IOException error) {
                    System.out.println(error.getMessage());
                }
                if (client != null) {
                    try {
                        BufferedReader input = new BufferedReader(new InputStreamReader(client.getInputStream()));
                        String caminhoArquivoNoServidor;
                        //Verifica se o caminho do arquivo no servidor é igual ao caminho do arquivo
                        //inserido pelo usuário
                        if ((caminhoArquivoNoServidor = input.readLine()) != null) {
                            File file = new File(caminhoArquivoNoServidor);
                            //Pega o tamnho do arquivo
                            byte[] fileBytes = new byte[(int)file.length()];
                            FileInputStream fileInputStream = new FileInputStream(file);
                            @SuppressWarnings("resource")
							BufferedInputStream bufferedInputStream = new BufferedInputStream(fileInputStream);
                            bufferedInputStream.read(fileBytes, 0, fileBytes.length);
                            ObjectOutputStream objectOutputStream = new ObjectOutputStream(client.getOutputStream());
                            objectOutputStream.writeObject(new FileDownload(ComandoEnum.FILEDOWNLOAD, null, fileBytes));
                            objectOutputStream.flush();
                        }
                    } catch (IOException error) {
                        try {
                            System.out.println(error.getMessage());
                            client.getOutputStream().close();
                            client.getInputStream().close();
                            client.close();
                        } catch (IOException error2) {
                            System.out.println(error2.getMessage());
                        }
                    }
                }
            }
        };
        this.threadFileDownloadSocket = new Thread(this.runnableFileDownloadSocket);
        this.threadFileDownloadSocket.start();
    }
    
    //Cria o runnable as threads que irão passar a lista de arquivos no servidor
    private void arrayFilesSocketComeceAOuvir(){
        this.runnableArrayFilesSocket = () -> {
            while (servidoresContinuemOuvindo) {
                Socket client = null;
                //Aceita o cliente
                try {
                    client = this.arrayFilesSocket.accept();
                } catch (IOException e) {
                    System.out.println(e.getMessage());
                }
                
                //Verifica se a conexão retorna um cliente não nulo
                if (client != null) {
                    try {
                        //Cria o ObjectOutputStream responsável por enviar oobjeto
                        ObjectOutputStream OOS = new ObjectOutputStream(client.getOutputStream());
                        //Recupera os nomes dos arquivos que estão no servidor
                        String[] listArq = recuperaArquivos();
                        //Cria objeto FileArray que seráenviado pelo socket
                        FileArray fileArray = new FileArray(ComandoEnum.FILEDOWNLOAD, null, listArq);
                        //Envia o objeto, da um flush para garantir que todos os dados foram enviados e fecha o ObjectOutputStream
                        OOS.writeObject(fileArray);
                        OOS.flush();
                        OOS.close();
                    } catch (IOException e) {
                        try {
                            client.getOutputStream().close();
                            client.close();
                        } catch (IOException ex) {
                            System.out.println(ex.getMessage());
                        }
                    }
                    
                }
            }
        };
        //Cria e inicia a thread
        this.threadArrayFilesSocket = new Thread(this.runnableArrayFilesSocket);
        this.threadArrayFilesSocket.start();
    }
    
    private InformacoesCliente registrarCliente(Comando comandoObj) {
        return this.informacoesServidor.addCliente(comandoObj.getInformacoesCliente());
    }
    
    //Para os runnables
    public void servidoresParemDeOuvir() {
        Iterator<ObjectOutputStream> iterador = outEuEOutrosClientes.iterator();
        //Desconecta todos os clientes conectados ao servidor
        for (int i = 0; iterador.hasNext(); i++) {
            try {
                ObjectOutputStream outOutroCliente = iterador.next();
                InformacoesCliente informacoesCliente = this.informacoesServidor.getCliente(i);
                outOutroCliente.writeObject(new Comando(ComandoEnum.DESCONECTAR, informacoesCliente));
                outOutroCliente.flush();
            } catch (IOException error) {
                System.out.println(error.getMessage());
            }
        }
        // Torna servidoresContinuemOuvindo como false, para que o loop de while das threads se torne inválido
        // e eleas parem de rodar
        this.servidoresContinuemOuvindo = false;
        try{
        this.mensagensSocket.close();
        this.fileDownloadSocket.close();
        this.fileUploadSocket.close();
        this.arrayFilesSocket.close();
        }catch (IOException ex){
            System.out.println(ex);
        }
    }
    
    //Remove cliente específico
    void desconectar(ObjectOutputStream outOutroCliente, InformacoesCliente cliente) {
        outEuEOutrosClientes.removeIf(c -> c.equals(outOutroCliente));
        this.informacoesServidor.removeCliente(cliente);
    }
    
    private String[] recuperaArquivos() {
        //Contador
        int cont = 0;

        //Pega a lista de arquivos e grava em um File
        File file = new File(informacoesServidor.getDiretorioArquivos());
        File[] arquivos = file.listFiles();

        //Variaveis String[] que irá grava os nomes dos arquivos
        String[] listaArquivos = new String[arquivos.length];

        //Loop para gravar o nome dos arquivos na variavel String[]
        for (File arquivo : arquivos) {
            listaArquivos[cont] = arquivo.getName();
            cont ++;
        }
        return listaArquivos;
    }
}
