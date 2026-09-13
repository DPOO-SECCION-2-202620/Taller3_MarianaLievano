package uniandes.dpoo.aerolinea.persistencia;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONObject;

import uniandes.dpoo.aerolinea.exceptions.AeropuertoDuplicadoException;
import uniandes.dpoo.aerolinea.exceptions.InformacionInconsistenteException;
import uniandes.dpoo.aerolinea.modelo.Aerolinea;
import uniandes.dpoo.aerolinea.modelo.Aeropuerto;
import uniandes.dpoo.aerolinea.modelo.Avion;
import uniandes.dpoo.aerolinea.modelo.Ruta;
import uniandes.dpoo.aerolinea.modelo.Vuelo;

public class PersistenciaAerolineaJson implements IPersistenciaAerolinea
{
    private static final String NOMBRE = "nombre";
    private static final String CODIGO = "codigo";
    private static final String NOMBRE_CIUDAD = "nombreCiudad";
    private static final String LATITUD = "latitud";
    private static final String LONGITUD = "longitud";
    private static final String CAPACIDAD = "capacidad";
    private static final String CODIGO_RUTA = "codigoRuta";
    private static final String ORIGEN = "origen";
    private static final String DESTINO = "destino";
    private static final String HORA_SALIDA = "horaSalida";
    private static final String HORA_LLEGADA = "horaLlegada";
    private static final String FECHA = "fecha";
    private static final String NOMBRE_AVION = "nombreAvion";

    @Override
    public void cargarAerolinea(String archivo, Aerolinea aerolinea) throws IOException, InformacionInconsistenteException
    {
        String jsonCompleto = new String(Files.readAllBytes(new File(archivo).toPath()));
        JSONObject raiz = new JSONObject(jsonCompleto);

        Map<String, Aeropuerto> aeropuertos = cargarAeropuertos(raiz.getJSONArray("aeropuertos"));
        Map<String, Avion> aviones = cargarAviones(aerolinea, raiz.getJSONArray("aviones"));

        cargarRutas(aerolinea, raiz.getJSONArray("rutas"), aeropuertos);
        cargarVuelos(aerolinea, raiz.getJSONArray("vuelos"), aviones);
    }

    @Override
    public void salvarAerolinea(String archivo, Aerolinea aerolinea) throws IOException
    {
        JSONObject jobject = new JSONObject();

        salvarAeropuertos(aerolinea, jobject);
        salvarAviones(aerolinea, jobject);
        salvarRutas(aerolinea, jobject);
        salvarVuelos(aerolinea, jobject);

        PrintWriter pw = new PrintWriter(archivo);
        jobject.write(pw, 2, 0);
        pw.close();
    }

    private Map<String, Aeropuerto> cargarAeropuertos(JSONArray jAeropuertos) throws InformacionInconsistenteException
    {
        Map<String, Aeropuerto> aeropuertos = new HashMap<String, Aeropuerto>();

        int numAeropuertos = jAeropuertos.length();

        for(int i = 0; i < numAeropuertos; i++)
        {
            JSONObject jAeropuerto = jAeropuertos.getJSONObject(i);

            String nombre = jAeropuerto.getString(NOMBRE);
            String codigo = jAeropuerto.getString(CODIGO);
            String nombreCiudad = jAeropuerto.getString(NOMBRE_CIUDAD);
            double latitud = jAeropuerto.getDouble(LATITUD);
            double longitud = jAeropuerto.getDouble(LONGITUD);

            try
            {
                Aeropuerto aeropuerto = new Aeropuerto(nombre, codigo, nombreCiudad, latitud, longitud);
                aeropuertos.put(codigo, aeropuerto);
            }
            catch(AeropuertoDuplicadoException e)
            {
                throw new InformacionInconsistenteException(e.getMessage());
            }
        }

        return aeropuertos;
    }

    private Map<String, Avion> cargarAviones(Aerolinea aerolinea, JSONArray jAviones)
    {
        Map<String, Avion> aviones = new HashMap<String, Avion>();

        int numAviones = jAviones.length();

        for(int i = 0; i < numAviones; i++)
        {
            JSONObject jAvion = jAviones.getJSONObject(i);

            String nombre = jAvion.getString(NOMBRE);
            int capacidad = jAvion.getInt(CAPACIDAD);

            Avion avion = new Avion(nombre, capacidad);

            aviones.put(nombre, avion);
            aerolinea.agregarAvion(avion);
        }

        return aviones;
    }

    private void cargarRutas(Aerolinea aerolinea, JSONArray jRutas, Map<String, Aeropuerto> aeropuertos) throws InformacionInconsistenteException
    {
        int numRutas = jRutas.length();

        for(int i = 0; i < numRutas; i++)
        {
            JSONObject jRuta = jRutas.getJSONObject(i);

            String codigoRuta = jRuta.getString(CODIGO_RUTA);
            String codigoOrigen = jRuta.getString(ORIGEN);
            String codigoDestino = jRuta.getString(DESTINO);
            String horaSalida = jRuta.getString(HORA_SALIDA);
            String horaLlegada = jRuta.getString(HORA_LLEGADA);

            Aeropuerto origen = aeropuertos.get(codigoOrigen);
            Aeropuerto destino = aeropuertos.get(codigoDestino);

            if(origen == null)
                throw new InformacionInconsistenteException("No existe el aeropuerto " + codigoOrigen);

            if(destino == null)
                throw new InformacionInconsistenteException("No existe el aeropuerto " + codigoDestino);

            Ruta ruta = new Ruta(origen, destino, horaSalida, horaLlegada, codigoRuta);
            aerolinea.agregarRuta(ruta);
        }
    }

    private void cargarVuelos(Aerolinea aerolinea, JSONArray jVuelos, Map<String, Avion> aviones) throws InformacionInconsistenteException
    {
        int numVuelos = jVuelos.length();

        for(int i = 0; i < numVuelos; i++)
        {
            JSONObject jVuelo = jVuelos.getJSONObject(i);

            String codigoRuta = jVuelo.getString(CODIGO_RUTA);
            String fecha = jVuelo.getString(FECHA);
            String nombreAvion = jVuelo.getString(NOMBRE_AVION);

            Ruta ruta = aerolinea.getRuta(codigoRuta);

            if(ruta == null)
                throw new InformacionInconsistenteException("No existe la ruta " + codigoRuta);

            if(!aviones.containsKey(nombreAvion))
                throw new InformacionInconsistenteException("No existe el avion " + nombreAvion);

            try
            {
                aerolinea.programarVuelo(fecha, codigoRuta, nombreAvion);
            }
            catch(Exception e)
            {
                throw new InformacionInconsistenteException(e.getMessage());
            }
        }
    }

    private void salvarAeropuertos(Aerolinea aerolinea, JSONObject jobject)
    {
        Map<String, Aeropuerto> aeropuertos = new HashMap<String, Aeropuerto>();

        for(Ruta ruta : aerolinea.getRutas())
        {
            Aeropuerto origen = ruta.getOrigen();
            Aeropuerto destino = ruta.getDestino();

            aeropuertos.put(origen.getCodigo(), origen);
            aeropuertos.put(destino.getCodigo(), destino);
        }

        JSONArray jAeropuertos = new JSONArray();

        for(Aeropuerto aeropuerto : aeropuertos.values())
        {
            JSONObject jAeropuerto = new JSONObject();

            jAeropuerto.put(NOMBRE, aeropuerto.getNombre());
            jAeropuerto.put(CODIGO, aeropuerto.getCodigo());
            jAeropuerto.put(NOMBRE_CIUDAD, aeropuerto.getNombreCiudad());
            jAeropuerto.put(LATITUD, aeropuerto.getLatitud());
            jAeropuerto.put(LONGITUD, aeropuerto.getLongitud());

            jAeropuertos.put(jAeropuerto);
        }

        jobject.put("aeropuertos", jAeropuertos);
    }

    private void salvarAviones(Aerolinea aerolinea, JSONObject jobject)
    {
        JSONArray jAviones = new JSONArray();

        for(Avion avion : aerolinea.getAviones())
        {
            JSONObject jAvion = new JSONObject();

            jAvion.put(NOMBRE, avion.getNombre());
            jAvion.put(CAPACIDAD, avion.getCapacidad());

            jAviones.put(jAvion);
        }

        jobject.put("aviones", jAviones);
    }

    private void salvarRutas(Aerolinea aerolinea, JSONObject jobject)
    {
        JSONArray jRutas = new JSONArray();

        for(Ruta ruta : aerolinea.getRutas())
        {
            JSONObject jRuta = new JSONObject();

            jRuta.put(CODIGO_RUTA, ruta.getCodigoRuta());
            jRuta.put(ORIGEN, ruta.getOrigen().getCodigo());
            jRuta.put(DESTINO, ruta.getDestino().getCodigo());
            jRuta.put(HORA_SALIDA, ruta.getHoraSalida());
            jRuta.put(HORA_LLEGADA, ruta.getHoraLlegada());

            jRutas.put(jRuta);
        }

        jobject.put("rutas", jRutas);
    }

    private void salvarVuelos(Aerolinea aerolinea, JSONObject jobject)
    {
        JSONArray jVuelos = new JSONArray();

        for(Vuelo vuelo : aerolinea.getVuelos())
        {
            JSONObject jVuelo = new JSONObject();

            jVuelo.put(CODIGO_RUTA, vuelo.getRuta().getCodigoRuta());
            jVuelo.put(FECHA, vuelo.getFecha());
            jVuelo.put(NOMBRE_AVION, vuelo.getAvion().getNombre());

            jVuelos.put(jVuelo);
        }

        jobject.put("vuelos", jVuelos);
    }
}