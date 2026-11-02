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
    
    
    
    
    private final String RUTA_USUARIOS = "datos/usuarios.txt";
    
    private void asegurarArchivo(String ruta) throws PersistenciaException {
        File archivo = new File(ruta);
        File carpeta = archivo.getParentFile();
        try {
            if (carpeta != null && !carpeta.exists()) {
                carpeta.mkdirs();
            }
            if (!archivo.exists()) {
                archivo.createNewFile();
            }
        } catch (IOException e) {
            throw new PersistenciaException("Error al crear la estructura en " + ruta, e);
        }
    }

    public List<String> leerUsuarios() throws PersistenciaException {
        List<String> lineas = new ArrayList<>();

        try (BufferedReader lector = new BufferedReader(
                new FileReader("datos/usuarios.txt", StandardCharsets.UTF_8))) {

            String linea;

            while ((linea = lector.readLine()) != null) {
                lineas.add(linea);
            }

            return lineas;

        } catch (IOException error) {
            throw new PersistenciaException(
                    "No se pudo leer datos/usuarios.txt", error
            );
        }
    }
    public void guardarUsuarios(List<String> lineas)
            
            throws PersistenciaException {
        asegurarArchivo(RUTA_USUARIOS);
        try (BufferedWriter escritor = new BufferedWriter(
            new FileWriter(RUTA_USUARIOS, StandardCharsets.UTF_8))) {
            
            for (String linea : lineas) {
                escritor.write(linea);
                escritor.newLine();
            }
           
        } catch (IOException e) {
            throw new PersistenciaException( // este método puede fallar con este tipo de error.
            "No se puede guardar " + RUTA_USUARIOS, e);
        }
                
    }
}