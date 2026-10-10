package es.uclm.esiiab.gps.biblioteca.usuario;

import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.Locale;
import java.util.regex.Pattern;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UsuarioService {

    private static final int ITERACIONES = 210_000;
    private static final int BITS_HASH = 256;
    private static final int BYTES_SAL = 16;
    private static final Pattern NOMBRE_VALIDO = Pattern.compile("[a-zA-Z0-9._-]{3,40}");
    private static final SecureRandom ALEATORIO = new SecureRandom();

    private final UsuarioRepository repositorio;

    public UsuarioService(UsuarioRepository repositorio) {
        this.repositorio = repositorio;
    }

    @Transactional
    public Usuario registrar(String nombreUsuario, String contrasena) {
        String nombre = normalizarNombre(nombreUsuario);
        validarContrasena(contrasena);
        if (repositorio.existsByNombreUsuarioIgnoreCase(nombre)) {
            throw new IllegalArgumentException("Ese nombre de usuario ya está registrado.");
        }
        return repositorio.save(new Usuario(nombre, crearHash(contrasena)));
    }

    @Transactional(readOnly = true)
    public Usuario autenticar(String nombreUsuario, String contrasena) {
        if (nombreUsuario == null || contrasena == null) {
            return null;
        }
        return repositorio.findByNombreUsuarioIgnoreCase(nombreUsuario.strip())
                .filter(usuario -> verificarHash(contrasena, usuario.getHashContrasena()))
                .orElse(null);
    }

    @Transactional(readOnly = true)
    public Usuario buscarPorId(Long id) {
        return repositorio.findById(id).orElse(null);
    }

    private String normalizarNombre(String nombreUsuario) {
        if (nombreUsuario == null || !NOMBRE_VALIDO.matcher(nombreUsuario.strip()).matches()) {
            throw new IllegalArgumentException(
                    "El nombre de usuario debe tener entre 3 y 40 caracteres: letras, números, punto, guion o guion bajo.");
        }
        return nombreUsuario.strip().toLowerCase(Locale.ROOT);
    }

    private void validarContrasena(String contrasena) {
        if (contrasena == null || contrasena.length() < 10 || contrasena.length() > 128) {
            throw new IllegalArgumentException("La contraseña debe tener entre 10 y 128 caracteres.");
        }
    }

    private String crearHash(String contrasena) {
        byte[] sal = new byte[BYTES_SAL];
        ALEATORIO.nextBytes(sal);
        byte[] hash = derivar(contrasena.toCharArray(), sal);
        return Base64.getEncoder().encodeToString(sal) + ":" + Base64.getEncoder().encodeToString(hash);
    }

    private boolean verificarHash(String contrasena, String hashAlmacenado) {
        try {
            String[] partes = hashAlmacenado.split(":", 2);
            byte[] sal = Base64.getDecoder().decode(partes[0]);
            byte[] hashEsperado = Base64.getDecoder().decode(partes[1]);
            byte[] hashActual = derivar(contrasena.toCharArray(), sal);
            return MessageDigest.isEqual(hashEsperado, hashActual);
        } catch (RuntimeException e) {
            return false;
        }
    }

    private byte[] derivar(char[] contrasena, byte[] sal) {
        PBEKeySpec especificacion = new PBEKeySpec(contrasena, sal, ITERACIONES, BITS_HASH);
        try {
            return SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
                    .generateSecret(especificacion).getEncoded();
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("No se pudo verificar la contraseña.", e);
        } finally {
            especificacion.clearPassword();
            java.util.Arrays.fill(contrasena, '\0');
        }
    }
}
