package com.cadastro.useCases.pedidos.impl;
import com.cadastro.entities.Clientes;
import com.cadastro.entities.Enderecos;
import com.cadastro.entities.Pedidos;
import com.cadastro.entities.Produtos;
import com.cadastro.frameWork.annotions.Business;
import com.cadastro.frameWork.utils.SenacException;
import com.cadastro.useCases.pedidos.PedidosBusiness;
import com.cadastro.useCases.pedidos.domanis.PedidosRequestDom;
import com.cadastro.useCases.pedidos.domanis.PedidosResponseDom;
import com.cadastro.useCases.pedidos.impl.mappers.PedidosMapper;
import com.cadastro.useCases.pedidos.impl.repositorys.PedidosClientesRepository;
import com.cadastro.useCases.pedidos.impl.repositorys.PedidosEnderecosRepository;
import com.cadastro.useCases.pedidos.impl.repositorys.PedidosRepository;
import com.cadastro.useCases.produtos.domanis.ProdutosResponseDom;
import com.cadastro.useCases.produtos.impl.mappers.ProdutosMapper;
import org.springframework.beans.factory.annotation.Autowired;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Business
public class PedidosBusinessImpl implements PedidosBusiness {
    @Autowired
    private PedidosRepository pedidosRepository;

    @Autowired
    private PedidosClientesRepository pedidosClientesRepository;

    @Autowired
    private PedidosEnderecosRepository pedidosEnderecosRepository;

    @Override
    public List<PedidosResponseDom> carregarPedidos() {
        List<Pedidos> pedidosList = pedidosRepository.findAll();

        List<PedidosResponseDom> out = pedidosList.stream()
                .map(PedidosMapper::pedidosToPedidosResponseDom)
                .collect(Collectors.toList());

        return out;

    }

    @Override
    public PedidosResponseDom criarPedido(PedidosRequestDom pedido) throws SenacException {
        List<String> messages = this.validacaoManutencaoPedido(pedido);

        if(!messages.isEmpty()){
            throw new SenacException(messages);
        }

        Optional<Clientes> cliente = pedidosClientesRepository.findById(pedido.getClienteId());
        if(!cliente.isPresent()){
            throw new SenacException("Cliente não encontrado");
        }

        Optional<Enderecos> endereco = pedidosEnderecosRepository.findById(pedido.getEnderecoId());
        if(!endereco.isPresent()){
            throw new SenacException("Endereço não encontrado");
        }

        Pedidos pedidoRetorno = pedidosRepository.save(PedidosMapper
                .pedidosResquestDomToPedidos(pedido, cliente.get(), endereco.get()));

        return PedidosMapper.pedidosToPedidosResponseDom(pedidoRetorno);

    }

    @Override
    public PedidosResponseDom atualizarPedido(Long id, PedidosRequestDom pedido) throws SenacException {
        List<String> messages = this.validacaoManutencaoPedido(pedido);

        if(!messages.isEmpty()){
            throw new SenacException(messages);
        }

        Optional<Clientes> cliente = pedidosClientesRepository.findById(pedido.getClienteId());
        if(!cliente.isPresent()){
            throw new SenacException("Cliente não encontrado");
        }

        Optional<Enderecos> endereco = pedidosEnderecosRepository.findById(pedido.getEnderecoId());
        if(!endereco.isPresent()){
            throw new SenacException("Endereço não encontrado");
        }

        Optional<Pedidos> pedidosRetorno = pedidosRepository.findById(id).map(record -> {
            record.setDataCriacao(pedido.getDataCriacao());
            record.setDataEntrega(pedido.getDataEntrega());
            record.setValorDesconto(pedido.getValorDesconto());
            record.setClienteId(cliente.get());
            record.setEnderecoId(endereco.get());
            return pedidosRepository.save(record);
        });

        if(pedidosRetorno.isPresent() == false){
            throw new SenacException("Pedido não encontrado");
        }

        PedidosResponseDom out = PedidosMapper.pedidosToPedidosResponseDom(pedidosRetorno.get());

        return out;
    }

    @Override
    public void deletarPedido(Long id) {
        pedidosRepository.deleteById(id);

    }

    @Override
    public PedidosResponseDom carregarPedidoById(Long id) throws SenacException {
        Optional<Pedidos> optionalPedidos = pedidosRepository.findById(id);
        if(!optionalPedidos.isPresent()) {
            throw new SenacException("Pedido não encontrado");
        }
        Pedidos pedidos = pedidosRepository.findById(id).get();
        PedidosResponseDom out = PedidosMapper.pedidosToPedidosResponseDom(pedidos);
        return out;

    }

    private List<String> validacaoManutencaoPedido(PedidosRequestDom pedido){
        List<String> messages = new ArrayList<>();

        if(pedido.getDataEntrega() == null){
            messages.add("Data de entrega não informada!");
        }

        if(pedido.getClienteId() == null || pedido.getClienteId() < 1){
            messages.add("Código do cliente não informado ou inválido!");
        }

        if(pedido.getEnderecoId() == null || pedido.getEnderecoId() < 1){
            messages.add("Código do endereço não informado ou inválido!");
        }

        return messages;
    }
}
