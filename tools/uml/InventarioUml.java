import com.google.gson.GsonBuilder;
import com.sun.source.tree.*;
import com.sun.source.util.*;
import javax.lang.model.element.TypeElement;
import javax.tools.ToolProvider;
import java.lang.reflect.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
import java.util.stream.Stream;

/** Inventario de tipos propios: contrasta AST Java y bytecode, sin inicializar clases. */
public final class InventarioUml {
    private static String tipo(Type t) {
        return t.getTypeName().replace("java.lang.", "").replace("java.util.", "")
                .replace("java.time.", "").replace('$', '.');
    }
    private static String vis(int m) {
        return Modifier.isPublic(m) ? "+" : Modifier.isProtected(m) ? "#" : Modifier.isPrivate(m) ? "-" : "~";
    }
    private static List<String> referencias(Type t) {
        List<String> r = new ArrayList<>();
        if (t instanceof Class<?> c) {
            if (c.isArray()) r.addAll(referencias(c.getComponentType()));
            else r.add(c.getName());
        } else if (t instanceof ParameterizedType p) {
            r.addAll(referencias(p.getRawType()));
            for (Type a : p.getActualTypeArguments()) r.addAll(referencias(a));
        } else if (t instanceof GenericArrayType a) r.addAll(referencias(a.getGenericComponentType()));
        else if (t instanceof WildcardType w) {
            for (Type b : w.getUpperBounds()) r.addAll(referencias(b));
            for (Type b : w.getLowerBounds()) r.addAll(referencias(b));
        }
        return r;
    }
    private static String parametros(Executable e) {
        List<String> p = new ArrayList<>();
        Parameter[] parametros = e.getParameters();
        // Nombre y ordinal de enum pertenecen al compilador, no al modelo fuente.
        int primero = e instanceof Constructor<?> && e.getDeclaringClass().isEnum() ? 2 : 0;
        for (int i = primero; i < parametros.length; i++) {
            Parameter a = parametros[i];
            p.add(a.getName() + " : " + tipo(a.getParameterizedType()));
        }
        return String.join(", ", p);
    }
    public static void main(String[] args) throws Exception {
        Path codigo = Path.of(args[0]);
        Path salida = Path.of(args[1]);
        List<Path> fuentes;
        try (Stream<Path> s = Files.walk(codigo.resolve("src"))) {
            fuentes = s.filter(p -> p.toString().endsWith(".java")).sorted().toList();
        }
        Set<String> declarados = new TreeSet<>();
        Map<String, Set<String>> usos = new TreeMap<>();
        var compiler = ToolProvider.getSystemJavaCompiler();
        try (var fm = compiler.getStandardFileManager(null, null, StandardCharsets.UTF_8)) {
            var archivos = fm.getJavaFileObjectsFromPaths(fuentes);
            String cp;
            // El compilador no expande comodines al recibir opciones via API.
            try (Stream<Path> jars = Files.list(codigo.resolve("lib"))) {
                cp = codigo.resolve("out") + java.io.File.pathSeparator
                        + String.join(java.io.File.pathSeparator, jars.filter(p -> p.toString().endsWith(".jar"))
                        .map(Path::toString).toList());
            }
            JavacTask task = (JavacTask) compiler.getTask(null, fm, null,
                    List.of("-proc:none", "-encoding", "UTF-8", "-classpath", cp), null, archivos);
            List<CompilationUnitTree> unidades = new ArrayList<>();
            task.parse().forEach(unidades::add);
            task.analyze();
            Trees trees = Trees.instance(task);
            for (CompilationUnitTree unidad : unidades) {
                new TreePathScanner<Void, Void>() {
                    private final Deque<String> nombres = new ArrayDeque<>();
                    @Override public Void visitClass(ClassTree node, Void unused) {
                        String simple = node.getSimpleName().toString();
                        if (simple.isEmpty()) return null; // clases anonimas no son clasificadores con nombre
                        String pkg = unidad.getPackageName() == null ? "" : unidad.getPackageName() + ".";
                        String nombre = nombres.isEmpty() ? pkg + simple : nombres.peek() + "$" + simple;
                        declarados.add(nombre);
                        usos.put(nombre, new TreeSet<>());
                        nombres.push(nombre);
                        super.visitClass(node, unused);
                        nombres.pop();
                        return null;
                    }
                    private void registrar() {
                        if (nombres.isEmpty()) return;
                        var elemento = trees.getElement(getCurrentPath());
                        if (elemento instanceof TypeElement t) usos.get(nombres.peek()).add(t.getQualifiedName().toString());
                    }
                    @Override public Void visitIdentifier(IdentifierTree n, Void p) {
                        registrar(); return super.visitIdentifier(n, p);
                    }
                    @Override public Void visitMemberSelect(MemberSelectTree n, Void p) {
                        registrar(); return super.visitMemberSelect(n, p);
                    }
                }.scan(unidad, null);
            }
        }
        List<Map<String, Object>> clases = new ArrayList<>();
        for (String nombre : declarados) {
            Class<?> c = Class.forName(nombre, false, InventarioUml.class.getClassLoader());
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("name", nombre);
            item.put("package", c.getPackageName());
            item.put("kind", c.isEnum() ? "enum" : c.isRecord() ? "record" : c.isInterface() ? "interface"
                    : Modifier.isAbstract(c.getModifiers()) ? "abstract class" : "class");
            item.put("modifiers", Modifier.toString(c.getModifiers()));
            item.put("superclass", c.getSuperclass() == null ? null : c.getSuperclass().getName());
            item.put("interfaces", Arrays.stream(c.getInterfaces()).map(Class::getName).sorted().toList());
            List<Map<String, Object>> campos = new ArrayList<>();
            for (Field f : Arrays.stream(c.getDeclaredFields()).sorted(Comparator.comparing(Field::getName)).toList()) {
                if (f.isSynthetic()) continue;
                campos.add(Map.of("name", f.getName(), "type", tipo(f.getGenericType()),
                        "visibility", vis(f.getModifiers()), "static", Modifier.isStatic(f.getModifiers()),
                        "final", Modifier.isFinal(f.getModifiers()), "enumConstant", f.isEnumConstant(),
                        "references", referencias(f.getGenericType())));
            }
            item.put("fields", campos);
            List<String> miembros = new ArrayList<>();
            for (Constructor<?> constructor : c.getDeclaredConstructors()) {
                if (c.isEnum() && constructor.getParameterCount() == 2) continue;
                if (!constructor.isSynthetic()) miembros.add(vis(constructor.getModifiers())
                        + c.getSimpleName() + "(" + parametros(constructor) + ")");
            }
            Set<String> dependencias = new TreeSet<>(usos.get(nombre));
            for (Method m : c.getDeclaredMethods()) {
                if (m.isSynthetic() || m.isBridge()) continue;
                String prefijo = Modifier.isStatic(m.getModifiers()) ? "{static} " : "";
                if (Modifier.isAbstract(m.getModifiers()) && !c.isInterface()) prefijo += "{abstract} ";
                List<String> variables = new ArrayList<>();
                for (TypeVariable<Method> v : m.getTypeParameters()) {
                    String limites = String.join(" & ", Arrays.stream(v.getBounds()).map(InventarioUml::tipo).toList());
                    variables.add(v.getName() + (limites.equals("Object") ? "" : " extends " + limites));
                }
                String genericos = variables.isEmpty() ? "" : "<" + String.join(", ", variables) + "> ";
                String excepciones = m.getExceptionTypes().length == 0 ? "" : " throws "
                        + String.join(", ", Arrays.stream(m.getExceptionTypes()).map(Class::getSimpleName).toList());
                miembros.add(prefijo + vis(m.getModifiers()) + genericos + m.getName() + "(" + parametros(m) + ") : "
                        + tipo(m.getGenericReturnType()) + excepciones);
                dependencias.addAll(referencias(m.getGenericReturnType()));
                for (Type t : m.getGenericParameterTypes()) dependencias.addAll(referencias(t));
                for (Class<?> t : m.getExceptionTypes()) dependencias.add(t.getName());
            }
            Collections.sort(miembros);
            item.put("operations", miembros);
            item.put("dependencies", dependencias);
            clases.add(item);
        }
        Set<String> bytecode = new TreeSet<>();
        try (Stream<Path> s = Files.walk(codigo.resolve("out"))) {
            for (Path p : s.filter(p -> p.toString().endsWith(".class")).toList()) {
                String nombre = codigo.resolve("out").relativize(p).toString()
                        .replace(java.io.File.separatorChar, '.').replaceAll("\\.class$", "");
                Class<?> c = Class.forName(nombre, false, InventarioUml.class.getClassLoader());
                if (!c.isAnonymousClass() && !c.isSynthetic() && !c.isLocalClass()) bytecode.add(nombre);
            }
        }
        if (!bytecode.equals(declarados)) throw new IllegalStateException("AST y bytecode difieren: "
                + bytecode + " / " + declarados);
        // Permisos productivos leídos del código compilado: evita una matriz duplicada en los diagramas.
        Class<?> tipoRol = Class.forName("modelo.TipoUsuario");
        Class<?> tipoAccion = Class.forName("gestor.Accion");
        Class<?> tipoPermisos = Class.forName("gestor.Permisos");
        Object permisos = tipoPermisos.getConstructor().newInstance();
        Method permite = tipoPermisos.getMethod("permite", tipoRol, tipoAccion);
        Map<String, List<String>> porRol = new TreeMap<>();
        for (Object rol : tipoRol.getEnumConstants()) {
            List<String> acciones = new ArrayList<>();
            for (Object accion : tipoAccion.getEnumConstants()) {
                if ((Boolean) permite.invoke(permisos, rol, accion)) acciones.add(accion.toString());
            }
            porRol.put(rol.toString().toLowerCase(Locale.ROOT), acciones);
        }
        Files.createDirectories(salida.getParent());
        Files.writeString(salida, new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create()
                .toJson(Map.of("sourceFiles", fuentes.size(), "declaredTypes", declarados.size(),
                        "coverage", "AST == named bytecode types", "classes", clases, "permissions", porRol)), StandardCharsets.UTF_8);
        System.out.println("Inventario verificado: " + fuentes.size() + " fuentes; " + clases.size() + " tipos con nombre.");
    }
}
