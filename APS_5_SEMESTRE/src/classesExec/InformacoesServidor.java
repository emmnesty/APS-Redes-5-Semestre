package classesExec;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;



public class InformacoesServidor extends Informacoes {
    //Cria objetos de outras classes
    private static InformacoesServidor instance;
    //Lista de InformacoesClientes
    private final List<InformacoesCliente> clientes;
    //Lista de ComandoMensagem
    private final List<ComandoMensagem> todasAsMensagens;
    private int ultimoIdAtribuido;
    
    private InformacoesServidor() {
        clientes = new ArrayList<>();
        todasAsMensagens = new ArrayList<>();
        ultimoIdAtribuido = 0;
    }
    
    //Se o objeto instance estiver vazio, ele instancia o objeto. O Synchronized
    //serve apra manter o controle, e permitir que s� uma thread chame o
    //m�todo por vez
    public static synchronized InformacoesServidor getInstance() {
        if (instance == null)
            instance = new InformacoesServidor();
        
        return instance;
    }
    
    // M�todo para adicionar cliente e setar o ID. Recebe um objeto InformacoesCliente,
    //d� um set no seu ID como o �ltimo ID + 1 (para ser um ID diferente), adiciona o novo
    //cliente a lista de clientes existente "Clientes".
    public InformacoesCliente addCliente(InformacoesCliente cliente) {
        cliente.setId(++ultimoIdAtribuido);
        clientes.add(cliente);
        return cliente;
    }
    
    // Uso do m�todo RemoveIf, que pode ser utilizado em Arrays Lists. Esse m�todo
    // remove elementos que satisfazem a condi��o especificada. Neste caso, remove
    // um elemento da lista "Clientes" se o ID do objeto "cliente" passado como 
    // par�metro for igual a algum presente na lista. Ou seja, ele vai procurar na lista
    // pelo ID recebido, vai acha-lo, e ent�o remover esse cliente da lista.
    public void removeCliente(InformacoesCliente cliente) {
        clientes.removeIf(c -> c.getId() == cliente.getId());
    }
    
    //Ele verifica se o index passado como par�metro � maior que -1 (para ser v�lido)
    // e se � menor que o tamanho da lista (tamb�m para ser v�lido). Caso seja, devolve
    // o cliente desse Index. Caso n�o seja encontrado, retorna null.
    public InformacoesCliente getCliente(int index) {
        if (index > -1 && index < clientes.size())
            return clientes.get(index);
        return null;
    }
    
    // Filtra o cliente por ID e encontra-o. 
    public InformacoesCliente getClienteById(int id) {
        if (id > 0) {
            Optional<InformacoesCliente> cliente = clientes.stream().filter(clienteF -> clienteF.getId() == id).findFirst();
            return cliente.isPresent() ? cliente.get() : null;
        }
        return null;
    }
    
    // Retorna o INDEX do cliente do ID passado como par�metro
    public int getIndexClienteById(int id) {
        if (id > 0) {
            return clientes.indexOf(getClienteById(id));
        }
        return -1;
    }
    
    //Pega o INDEX do objeto passado como par�metro. Se encontrar devolve o INDEX, se n�o
    //devolve -1.
    public int getIndexCliente(InformacoesCliente cliente) {
        if (cliente != null) {
            return clientes.indexOf(cliente);
        }
        return -1;
    }
   
    // Adiciona um "ComandoMensagem" para a lista de ComandoMensagem chamada "todasAsMensagens"
    public void addParaTodasAsMensagens(ComandoMensagem mensagem) {
        todasAsMensagens.add(mensagem);
    }
    
    //Pega e devolve a lista todasAsMensagens
    public List<ComandoMensagem> getTodasAsMensagens() {
        return todasAsMensagens;
    }
    
    //Filtra a lista inteira, fazendo um filtro que corresponda ao par�metro dos IDs passados,
    //ent�o retornando as mensagens aceitas.
    public List<ComandoMensagem> getTodasAsMensagensDeParaOuParaDe(int idDe, int idPara) {
        return todasAsMensagens.stream().filter(cmd -> (cmd.getMensagemDe() == idDe && cmd.getMensagemPara() == idPara) || (cmd.getMensagemDe() == idPara && cmd.getMensagemPara() == idDe)).collect(Collectors.toCollection(ArrayList::new));
    }
    
    //Pega todas as mensagens recebidas por um ID
    public List<ComandoMensagem> getTodasAsMensagensPara(int idPara) {
        return todasAsMensagens.stream().filter(cmd -> cmd.getMensagemPara() == idPara).collect(Collectors.toCollection(ArrayList::new));
    }
    
    //Remove todas as mensagens de um ID
    public void removerDeTodasAsMensagensOndeDeOuParaSeja(int idDeOuPara) {
        todasAsMensagens.removeIf(cmd -> cmd.getMensagemDe() == idDeOuPara || cmd.getMensagemPara() == idDeOuPara);
    }
}
