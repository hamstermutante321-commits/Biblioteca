package persistencia;

import excepciones.PersistenciaException;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.io.File;
import java.io.FileWriter;


public class ArchivosDatos {

    private final String rutaUsuarios;

    public ArchivosDatos() {
        this("datos");
    }

    // Permite usar otra carpeta en las pruebas.
    public ArchivosDatos(String carpetaDatos) {
        rutaUsuarios = carpetaDatos + "/usuarios.txt";
    }
    
    private void asegurarArchivo(String ruta) throws PersistenciaException {
        File archivo = new File(ruta);
        File carpeta = archivo.getParentFile();
        try {
            if (!carpeta.exists()) {
                if (!carpeta.mkdirs()) {
                    throw new IOException("No se pudo crear la carpeta " + carpeta);
                }
            }
            if (!archivo.exists()) {
                archivo.createNewFile();
            }
        } catch (IOException e) {
            throw new PersistenciaException("Error al crear la estructura en " + ruta, e);
        }
    }

    public List<String> leerUsuarios() throws PersistenciaException {
        asegurarArchivo(rutaUsuarios);
        List<String> lineas = new ArrayList<>();
        BufferedReader lector = null;

        try {
            lector = new BufferedReader(
                    new FileReader(rutaUsuarios, StandardCharsets.UTF_8));

            String linea;

            while ((linea = lector.readLine()) != null) {
                lineas.add(linea);
            }

            return lineas;

        } catch (IOException error) {
            throw new PersistenciaException(
                    "No se pudo leer " + rutaUsuarios, error
            );
        } finally {
            // Se cierra el lector aunque ocurra un error o se ejecute el return.
            if (lector != null) {
                try {
                    lector.close();
                } catch (IOException error) {
                    throw new PersistenciaException("No se pudo cerrar el lector", error);
                }
            }
        }
    }
    // Reemplaza el contenido del TXT con la lista completa.
    public void guardarUsuarios(List<String> lineas) throws PersistenciaException {
        asegurarArchivo(rutaUsuarios);
        try (BufferedWriter escritor = new BufferedWriter(
                new FileWriter(rutaUsuarios, StandardCharsets.UTF_8))) {
            
            for (String linea : lineas) {
                escritor.write(linea);
                escritor.newLine();
            }
           
        } catch (IOException e) {
            throw new PersistenciaException(
                    "No se puede guardar " + rutaUsuarios, e);
        }
                
    }
}
