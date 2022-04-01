package classesExec;

//Importação de bibliotecas/pacotes/classes utilizados
import Enum.ComandoEnum;
import Telas.ClienteScreen;
import java.awt.HeadlessException;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.JOptionPane;


public class ClienteSocket {
    //Atributos da classe
    private final Socket socket;
    private final InformacoesCliente informacoesCliente;
    private final ClienteScreen clienteScreen;
    private final List<InformacoesCliente> listaClientes;
    private final ObjectOutputStream out;
    private ObjectInputStream in;
    //private ObjectInputStream inArray;
    
    //Construtor da classe
    public ClienteSocket(InformacoesCliente infoCliente, ClienteScreen clienteScreen, Socket socket) throws IOException {
        this.informacoesCliente = infoCliente;
        this.clienteScreen = clienteScreen;
        this.listaClientes = new ArrayList<>();
        this.socket = socket;
        this.out = new ObjectOutputStream(socket.getOutputStream());
        registrarCliente();
        comecarAOuvir();
    }
    
    //Registra o cliente
    private void registrarCliente() {
        enviarMensagem(ComandoEnum.REGISTRAR);
    }
    
    private void comecarAOuvir() {
        //Cria método runnable usando expressão Lambda
        Runnable runnableListen = () -> {
            try {
                this.in = new ObjectInputStream(this.socket.getInputStream());
                Comando comandoObj;
                while ((comandoObj = (Comando) in.readObject()) != null) {
                    if (comandoObj.getComando() != null) {
                        switch (comandoObj.getComando()) {
                            //Ações que os comandos (especificados em ComandoEnum) tem sob essa classe
                            //caso forem chamados
                            
                            //Adiciona cliente a lista e tela
                            case ADICIONAR:
                                listaClientes.add(comandoObj.getInformacoesCliente());
                                clienteScreen.adicionarClienteAListaClientes(comandoObj.getInformacoesCliente());
                                break;
                            //Remove cliente da lista e da tela    
                            case REMOVER:
                                listaClientes.remove(comandoObj.getInformacoesCliente());
                                clienteScreen.removerClienteDaListaClientes(comandoObj.getInformacoesCliente());
                                break;
                            //Desconecta o cliente do chat
                            case DESCONECTAR:
                                clienteScreen.desconectarAMim();
                                break;
                            //Seta o ID e registra o título do chat de um cliente X na tela
                            case REGISTRAR:
                                informacoesCliente.setId(comandoObj.getInformacoesCliente().getId());
                                clienteScreen.registrarAMim(informacoesCliente);
                                break;
                            case MENSAGEM:
                            //Abre um jOptionPane de alerta para todos os clientes
                            case MENSAGEMALERTA:
                                if (comandoObj instanceof ComandoMensagem) {
                                    ComandoMensagem comandoMensagemObj = ((ComandoMensagem) comandoObj);
                                    if (comandoObj.getComando() == ComandoEnum.MENSAGEM)
                                        clienteScreen.adicionarMensagemNoCampoDeMensagens(comandoMensagemObj);
                                    else
                                        JOptionPane.showMessageDialog(null, comandoMensagemObj.getMensagem(), "Alerta!", JOptionPane.WARNING_MESSAGE);
                                }
                                break;
                        }
                    }
                }
            } catch (IOException | ClassNotFoundException error) {
                System.out.println(error.getMessage());
            }
        };
        Thread threadListen = new Thread(runnableListen);
        threadListen.start();
    }
    
    //Registra o cliente
    public void enviarMensagem(ComandoEnum comando) {
        try {
            Comando mensagemObj = new Comando(comando, informacoesCliente);
            out.writeObject(mensagemObj);
            out.flush();
        } catch (IOException error) {
            JOptionPane.showMessageDialog(null, error.getMessage(), "Erro ao enviar mensagem.", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    //Envia mensagem de um cliente para outro, ou de um para todos
    public void enviarMensagem(ComandoEnum comando, int mensagemDe, int mensagemPara, String mensagem) {
        try {
            Comando mensagemObj = new ComandoMensagem(comando, informacoesCliente, mensagemDe, mensagemPara, mensagem + "\n");
            out.writeObject(mensagemObj);
            out.flush();
            
        } catch (IOException error) {
            JOptionPane.showMessageDialog(null, error.getMessage(), "Erro ao enviar mensagem.", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    //Envia um arquivo
    public void transferirArquivo(int idSelecionadoDaListaClientes, File file) {
        try {
            //Pega os IP local do servidor
            byte[] ipLocalByteArr = InetAddress.getByName(informacoesCliente.getIpLocalServidor()).getAddress();
            Socket socketTransferirArquivo = new Socket(InetAddress.getByAddress(informacoesCliente.getIpPublicoServidor(), ipLocalByteArr), informacoesCliente.getPortFileUpload());
            byte[] fileBytes = new byte[(int)file.length()];
            //Joga a file passada como parametro em um input stream
            FileInputStream fileInputStream = new FileInputStream(file);
            BufferedInputStream bufferedInputStream = new BufferedInputStream(fileInputStream);
            bufferedInputStream.read(fileBytes, 0, fileBytes.length);
            //Realiza o Upload com as informações coletadas até o momento
            FileUpload fileUpload = new FileUpload(ComandoEnum.FILEUPLOAD, informacoesCliente, fileBytes, file.getName(), idSelecionadoDaListaClientes);
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(socketTransferirArquivo.getOutputStream());
            objectOutputStream.writeObject(fileUpload);
            objectOutputStream.flush();
            out.writeObject(fileUpload);
            out.flush();
        } catch (FileNotFoundException error) {
            JOptionPane.showMessageDialog(null, error.getMessage(), "Arquivo não encontrado.", JOptionPane.ERROR_MESSAGE);
        } catch (IOException error) {
            JOptionPane.showMessageDialog(null, error.getMessage(), "IOException", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    //Baixa arquivo
    public void baixarArquivo(String caminhoArquivoServidor, String localParaSalvar) {
        //Pega a string que está salva
        String arquivoSemDiretorio = caminhoArquivoServidor.replace(informacoesCliente.getDiretorioArquivos(), "");
        //Cria o padrão de linguagem formal
        Pattern extrairApenasONomeDoArquivo = Pattern.compile("_.{1,}$");
        //Verifica se o que da dentro da arquivoSemDiretorio possui o padrão mencionado
        Matcher matcher = extrairApenasONomeDoArquivo.matcher(arquivoSemDiretorio);
        String nomeArquivo = "";
        if (matcher.find())
            nomeArquivo = matcher.group();
        nomeArquivo = nomeArquivo.substring(1);
        try {
            //Pega os Ips
            byte[] ipLocalByteArr = InetAddress.getByName(informacoesCliente.getIpLocalServidor()).getAddress();
            Socket socketDownloadArquivo = new Socket(InetAddress.getByAddress(informacoesCliente.getIpPublicoServidor(), ipLocalByteArr), informacoesCliente.getPortFileDownload());
            PrintWriter printWriter = new PrintWriter(socketDownloadArquivo.getOutputStream());
            printWriter.println(caminhoArquivoServidor);
            printWriter.flush();
            
            //ObjectInputStream para fazer o download do arquivo
            ObjectInputStream objectInputStream = new ObjectInputStream(socketDownloadArquivo.getInputStream());
            FileDownload fileDownload = (FileDownload) objectInputStream.readObject();
            //Se não existir local para salvar, cria um diretório
            if (!new File(localParaSalvar).exists()) {
                new File(localParaSalvar).mkdirs();
            }
            //"Formato semântico" para salvar no computador (\\talpasta\\talarquivo.txt)
            nomeArquivo = localParaSalvar + "\\" + nomeArquivo;
            new File(nomeArquivo).createNewFile();
            //Baixa o arquivo
            try (OutputStream outputStream = new FileOutputStream(nomeArquivo)) {
                outputStream.write(fileDownload.getFileBytes());
            }
            JOptionPane.showMessageDialog(null, "O arquivo foi baixado com sucesso! " + nomeArquivo, "Sucesso ao baixar arquivo.", JOptionPane.INFORMATION_MESSAGE);
        } catch (HeadlessException | IOException | ClassNotFoundException error) {
            JOptionPane.showMessageDialog(null, error.getMessage(), "Erro ao baixar arquivo.", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    //Recupera String[] com lista de arquivos no servidor
    public String[] recuperaArquivosNoServidor(){
        //Objeto FileArray que ira ser recebido
        FileArray fileArray = null;
        try {
            //Pega os Ips e cria socket para estabelecer conexão
            byte[] ipLocalByteArr = InetAddress.getByName(informacoesCliente.getIpLocalServidor()).getAddress();
            Socket socketArrayList = new Socket(InetAddress.getByAddress(informacoesCliente.getIpPublicoServidor(), ipLocalByteArr), informacoesCliente.getPortArrayFiles());
            
            //Cria um ObjectInputStream para receber o objeto e depois o grava em fileArray
            ObjectInputStream OIS = new ObjectInputStream(socketArrayList.getInputStream());
            fileArray = (FileArray) OIS.readObject();
            
        } catch (IOException | ClassNotFoundException e) {
            System.out.println(e.getMessage());
        }
        //retorna oString[] com os nomes dos arquivos
        return fileArray.getNomeArquivos();
    }
    //Métodos Get
    public List<InformacoesCliente> getListaClientes() {
        return this.listaClientes;
    }
    
    public InformacoesCliente getClienteDaListaClientes(int index) {
        if (index > -1 && index < this.listaClientes.size())
            return this.listaClientes.get(index);
        return null;
    }
}
