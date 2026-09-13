package uniandes.dpoo.aerolinea.modelo.tarifas;

import uniandes.dpoo.aerolinea.modelo.Vuelo;
import uniandes.dpoo.aerolinea.modelo.cliente.Cliente;
import uniandes.dpoo.aerolinea.modelo.cliente.ClienteCorporativo;
import uniandes.dpoo.aerolinea.modelo.cliente.ClienteNatural;

public class CalculadoraTarifasTemporadaBaja extends CalculadoraTarifas
{
    protected final int COSTO_POR_KM_NATURAL = 600;
    protected final int COSTO_POR_KM_CORPORATIVO = 900;

    protected final double DESCUENTO_PEQ = 0.02;
    protected final double DESCUENTO_MEDIANAS = 0.10;
    protected final double DESCUENTO_GRANDES = 0.20;

    @Override
    public int calcularCostoBase(
            Vuelo vuelo, Cliente cliente )
    {
        int distancia =
                calcularDistanciaVuelo( vuelo.getRuta( ) );

        if( ClienteNatural.NATURAL.equals(
                cliente.getTipoCliente() ) )
        {
            return COSTO_POR_KM_NATURAL*distancia;
        }

        return COSTO_POR_KM_CORPORATIVO * distancia;
    }

    @Override
    public double calcularPorcentajeDescuento(Cliente cliente)
    {
        if(!(cliente instanceof ClienteCorporativo))
        {
            return 0;
        }

        ClienteCorporativo corporativo =
                (ClienteCorporativo)cliente;

        if( corporativo.getTamanoEmpresa()
                == ClienteCorporativo.GRANDE)
        {
            return DESCUENTO_GRANDES;
        }

        else if(corporativo.getTamanoEmpresa()
                == ClienteCorporativo.MEDIANA )
        {
            return DESCUENTO_MEDIANAS;
        }

        else if(corporativo.getTamanoEmpresa( )
                ==ClienteCorporativo.PEQUENA )
        {
            return DESCUENTO_PEQ;
        }

        return 0;
    }
}