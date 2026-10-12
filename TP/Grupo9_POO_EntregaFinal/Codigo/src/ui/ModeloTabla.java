package ui;

import javax.swing.table.DefaultTableModel;

/*
 * Modelo de tabla de solo lectura.
 *
 * Declara el tipo de cada columna para que, al ordenar por una columna
 * numerica como el ID, el orden sea numerico y no alfabetico.
 */
public class ModeloTabla extends DefaultTableModel {

    private final Class<?>[] tipos;

    public ModeloTabla(String[] columnas, Class<?>[] tipos) {
        super(columnas, 0);
        this.tipos = tipos;
    }

    @Override
    public boolean isCellEditable(int fila, int columna) {
        return false;
    }

    @Override
    public Class<?> getColumnClass(int columna) {
        return tipos[columna];
    }
}
