package classesExec;

import static classesExec.ServidoresSockets.outEuEOutrosClientes;
import java.io.IOException;
import java.net.Socket;
import java.util.Iterator;
import Enum.ComandoEnum;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class ThreadMensagens extends Thread {
    private final Socket socketEuClient;
    private final InformacoesCliente informacoesEuCliente;
    private final ServidoresSockets servidorSocket;
    private final ObjectInputStream in;
    private final ObjectOutputStream out;

    //Construtor da classe
    ThreadMensagens(Socket client, InformacoesCliente cliente, ServidoresSockets servidorSocket, ObjectInputStream in, ObjectOutputStream out) {
        this.socketEuClient = client;
        this.informacoesEuCliente = cliente;
        this.servidorSocket = servidorSocket;
        this.in = in;
        this.out = out;
    }
    
    public void run() {
        try {
            Comando comandoObj;
            ComandoMensagem comandoMensagem;
            while ((comandoObj = (Comando) in.readObject()) != null) {
                InformacoesServidor informacoesServidor = InformacoesServidor.getInstance();
                Iterator<ObjectOutputStream> iterador = outEuEOutrosClientes.iterator();
                if (comandoObj.getComando() != null) {
                    //Ações que os comandos (especificados em ComandoEnum) tem sob essa classe
                    //caso forem chamados
                    switch (comandoObj.getComando()) {
                        //Remove tudo relecionado a cliente de ID X
                        case SAIR:
                            while (iterador.hasNext()) {
                                ObjectOutputStream outEuEOutroClient = iterador.next();
                                outEuEOutroClient.writeObject(new Comando(ComandoEnum.REMOVER, informacoesEuCliente));
                            }   
                            informacoesServidor.removerDeTodasAsMensagensOndeDeOuParaSeja(informacoesEuCliente.getId());
                            this.servidorSocket.desconectar(this.out, this.informacoesEuCliente);
                            this.socketEuClient.close();
                            break;
                        case RECARREGAR:
                            if (comandoObj instanceof ComandoMensagem) {
                                comandoMensagem = ((ComandoMensagem) comandoObj);
                                List<ComandoMensagem> mensagens;
                                if (comandoMensagem.getMensagemPara() == 0) {
                                    mensagens = informacoesServidor.getTodasAsMensagensPara(0);
                                } else {
                                    mensagens = informacoesServidor.getTodasAsMensagensDeParaOuParaDe(comandoMensagem.getMensagemDe(), comandoMensagem.getMensagemPara());
                                }
                                for (int i = 0, l = mensagens.size(); i < l; i++) {
                                    out.writeObject(mensagens.get(i));
                                    out.flush();
                                }
                            }
                            break;
                        //Mensagem de alerta para todos os clientes conectados
                        case MENSAGEM:
                        case MENSAGEMALERTA:
                            if (comandoObj instanceof ComandoMensagem) {
                                comandoMensagem = ((ComandoMensagem) comandoObj);
                                boolean comAlerta = comandoObj.getComando() == ComandoEnum.MENSAGEMALERTA;
                                comandoMensagem.setMensagem("(" + informacoesEuCliente.getId() + ") " + informacoesEuCliente.getNome() + " diz: " + comandoMensagem.getMensagem());
                                System.out.println("\r\n" + comandoMensagem.getMensagem());
                                if (!comAlerta)
                                    informacoesServidor.addParaTodasAsMensagens(comandoMensagem);
                                if (comandoMensagem.getMensagemPara() == 0) {
                                    while (iterador.hasNext()) {
                                        ObjectOutputStream outEuEOutroCliente = iterador.next();
                                        outEuEOutroCliente.writeObject(comandoMensagem);
                                        outEuEOutroCliente.flush();
                                    }
                                } else {
                                    InformacoesCliente outroCliente = informacoesServidor.getClienteById(comandoMensagem.getMensagemPara());
                                    if (outroCliente != null) {
                                        out.writeObject(comandoMensagem);
                                        out.flush();

                                        int index = informacoesServidor.getIndexCliente(outroCliente);
                                        if (index > -1 && index < outEuEOutrosClientes.size()) {
                                            ObjectOutputStream outOutroCliente = outEuEOutrosClientes.get(index);
                                            outOutroCliente.writeObject(comandoMensagem);
                                            outOutroCliente.flush();
                                        }
                                    }
                                }
                            }
                            break;
                        //Envio de arquivo
                        case FILEUPLOAD:
                            if (comandoObj instanceof FileUpload) {
                                FileUpload fileUpload = (FileUpload) comandoObj;
                                String nomeArquivoNoServidor = fileUpload.getCurrentTimeMillis() + "_" + fileUpload.getNome();
                                //Cria o padrão de linguagem formal
                                Pattern extrairApenasONomeDoArquivo = Pattern.compile("_.{1,}$");
                                //Verifica se o que da dentro da arquivoSemDiretorio possui o padrão mencionado
                                Matcher matcher = extrairApenasONomeDoArquivo.matcher(nomeArquivoNoServidor);
                                String nomeArquivo = "";
                                if (matcher.find())
                                    nomeArquivo = matcher.group();
                                nomeArquivo = nomeArquivo.substring(1);
                                String caminhoArquivoNoServidor = informacoesServidor.getDiretorioArquivos() + nomeArquivoNoServidor;
                                String mensagem = "(" + informacoesEuCliente.getId() + ") " + informacoesEuCliente.getNome() + " enviou o arquivo " + nomeArquivo + "\n";
                                ComandoMensagem mensagemObj = new ComandoMensagem(ComandoEnum.MENSAGEM, informacoesEuCliente, informacoesEuCliente.getId(), fileUpload.getArquivoPara(), mensagem);
                                informacoesServidor.addParaTodasAsMensagens(mensagemObj);
                                if (fileUpload.getArquivoPara() == 0) {
                                    while (iterador.hasNext()) {
                                        ObjectOutputStream outEuOuOutroCliente = iterador.next();
                                        outEuOuOutroCliente.writeObject(mensagemObj);
                                        outEuOuOutroCliente.flush();
                                    }
                                } else {
                                    InformacoesCliente outroCliente = informacoesServidor.getClienteById(fileUpload.getArquivoPara());
                                    if (outroCliente != null) {
                                        out.writeObject(mensagemObj);
                                        out.flush();

                                        int index = informacoesServidor.getIndexCliente(outroCliente);
                                        if (index > -1 && index < outEuEOutrosClientes.size()) {
                                            ObjectOutputStream outOutroCliente = outEuEOutrosClientes.get(index);
                                            outOutroCliente.writeObject(mensagemObj);
                                            outOutroCliente.flush();
                                        }
                                    }
                                }
                            }
                            break;
                    }
                }
            }
        } catch (IOException | ClassNotFoundException error) {
            System.out.println(error.getMessage());
        }
    }
}
