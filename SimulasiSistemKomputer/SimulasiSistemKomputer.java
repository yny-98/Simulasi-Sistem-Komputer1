
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;

public class SimulasiSistemKomputer extends JFrame {

    Path root = Paths.get("SimulasiKomputer").toAbsolutePath();

    JTextField angka1 = new JTextField("10", 5);
    JTextField angka2 = new JTextField("5", 5);
    JComboBox<String> operasi =
            new JComboBox<>(new String[]{"+", "-", "*", "/"});

    JTextArea hasil = new JTextArea();

    JTextField namaFile = new JTextField(12);
    JTextField namaBaru = new JTextField(12);

    JComboBox<String> jenis =
            new JComboBox<>(new String[]{"File", "Folder"});

    DefaultTableModel fileModel;
    DefaultTableModel prosesModel;

    JTable tabelFile;

    public SimulasiSistemKomputer() {

        setTitle("SIMULASI SISTEM KOMPUTER");
        setSize(850, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        try {
            Files.createDirectories(root);
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, e.getMessage());
        }

        JTabbedPane tab = new JTabbedPane();

        tab.addTab("Simulasi Sistem", panelSimulasi());
        tab.addTab("Manajemen File", panelFile());
        tab.addTab("Proses Aplikasi", panelProses());

        add(tab);
    }

    // =================================
    // FITUR 1: SIMULASI SISTEM KOMPUTER
    // =================================

    JPanel panelSimulasi() {

        JPanel panel = new JPanel(new BorderLayout(10, 10));

        JPanel input = new JPanel();

        input.add(new JLabel("Angka 1:"));
        input.add(angka1);

        input.add(new JLabel("Angka 2:"));
        input.add(angka2);

        input.add(new JLabel("Operasi:"));
        input.add(operasi);

        JButton tombol = new JButton("Jalankan");

        input.add(tombol);

        hasil.setEditable(false);
        hasil.setFont(new Font("Monospaced", Font.PLAIN, 14));

        panel.add(input, BorderLayout.NORTH);
        panel.add(new JScrollPane(hasil), BorderLayout.CENTER);

        tombol.addActionListener(e -> simulasi());

        return panel;
    }

    void simulasi() {

        try {

            double a = Double.parseDouble(angka1.getText());
            double b = Double.parseDouble(angka2.getText());

            String op = (String) operasi.getSelectedItem();

            double total;

            hasil.setText("");

            hasil.append("===== INPUT =====\n");
            hasil.append("Angka 1: " + a + "\n");
            hasil.append("Angka 2: " + b + "\n");
            hasil.append("Operasi: " + op + "\n\n");

            hasil.append("===== CPU: CONTROL UNIT =====\n");
            hasil.append("CU mengatur jalannya instruksi.\n\n");

            hasil.append("===== CPU: ALU =====\n");

            switch (op) {
                case "+":
                    total = a + b;
                    break;

                case "-":
                    total = a - b;
                    break;

                case "*":
                    total = a * b;
                    break;

                case "/":
                    if (b == 0) {
                        throw new ArithmeticException(
                                "Tidak bisa membagi dengan nol!");
                    }
                    total = a / b;
                    break;

                default:
                    return;
            }

            hasil.append("ALU melakukan perhitungan.\n\n");

            hasil.append("===== MEMORY =====\n");
            hasil.append("Hasil disimpan: " + total + "\n\n");

            hasil.append("===== OUTPUT =====\n");
            hasil.append("HASIL = " + total);

        } catch (NumberFormatException e) {

            pesan("Masukkan angka yang benar!");

        } catch (ArithmeticException e) {

            pesan(e.getMessage());
        }
    }

    // =================================
    // FITUR 2: MANAJEMEN FILE
    // =================================

    JPanel panelFile() {

        JPanel panel = new JPanel(new BorderLayout(10, 10));

        // Tabel daftar file

        fileModel = new DefaultTableModel(
                new String[]{"Nama File/Folder", "Jenis", "Lokasi"}, 0
        ) {
            public boolean isCellEditable(int row, int col) {
                return false;
            }
        };

        tabelFile = new JTable(fileModel);
        tabelFile.setRowHeight(25);

        JScrollPane scroll = new JScrollPane(tabelFile);

        // Form input

        JPanel form = new JPanel(new GridLayout(2, 2, 5, 5));

        form.setBorder(
                BorderFactory.createEmptyBorder(10, 10, 10, 10)
        );

        form.add(new JLabel("Jenis:"));
        form.add(jenis);

        form.add(new JLabel("Nama File/Folder:"));
        form.add(namaFile);

        // Tombol

        JButton buat = new JButton("Buat");
        JButton rename = new JButton("Rename");
        JButton copy = new JButton("Copy");
        JButton delete = new JButton("Delete");
        JButton refresh = new JButton("Refresh");

        JPanel tombol = new JPanel();

        tombol.add(buat);
        tombol.add(rename);
        tombol.add(copy);
        tombol.add(delete);
        tombol.add(refresh);

        // Kolom nama baru

        JPanel bawah = new JPanel(new BorderLayout());

        JPanel renamePanel = new JPanel();

        renamePanel.add(new JLabel("Nama Baru / Tujuan:"));
        renamePanel.add(namaBaru);

        bawah.add(renamePanel, BorderLayout.NORTH);
        bawah.add(tombol, BorderLayout.SOUTH);

        panel.add(form, BorderLayout.NORTH);
        panel.add(scroll, BorderLayout.CENTER);
        panel.add(bawah, BorderLayout.SOUTH);

        buat.addActionListener(e -> buatFile());
        rename.addActionListener(e -> renameFile());
        copy.addActionListener(e -> copyFile());
        delete.addActionListener(e -> deleteFile());
        refresh.addActionListener(e -> tampilkanFile());

        tabelFile.getSelectionModel().addListSelectionListener(e -> {

            if (!e.getValueIsAdjusting()) {

                int baris = tabelFile.getSelectedRow();

                if (baris >= 0) {
                    namaFile.setText(
                            fileModel.getValueAt(baris, 2).toString()
                    );
                }
            }
        });

        tampilkanFile();

        return panel;
    }

    // Menampilkan daftar file dan folder

    void tampilkanFile() {

        fileModel.setRowCount(0);

        try (DirectoryStream<Path> daftar =
                     Files.newDirectoryStream(root)) {

            for (Path p : daftar) {

                String nama = p.getFileName().toString();

                String tipe = Files.isDirectory(p)
                        ? "Folder" : "File";

                fileModel.addRow(new Object[]{
                        nama,
                        tipe,
                        nama
                });
            }

        } catch (IOException e) {

            pesan("Gagal membaca folder!");
        }
    }

    // Memastikan lokasi tetap di dalam folder aplikasi

    Path pathAman(String nama) {

        Path p = root.resolve(nama).normalize();

        if (!p.startsWith(root) || p.equals(root)) {
            throw new IllegalArgumentException(
                    "Lokasi tidak valid!"
            );
        }

        return p;
    }

    // Membuat file atau folder

    void buatFile() {

        try {

            Path p = pathAman(namaFile.getText());

            if (jenis.getSelectedItem().equals("Folder")) {

                Files.createDirectories(p);

            } else {

                Files.createFile(p);
            }

            tampilkanFile();

            pesan("Berhasil membuat file/folder!");

        } catch (Exception e) {

            pesan("Gagal: " + e.getMessage());
        }
    }

    // Rename

    void renameFile() {

        try {

            Path lama = pathAman(namaFile.getText());
            Path baru = pathAman(namaBaru.getText());

            Files.move(lama, baru);

            tampilkanFile();

            pesan("Berhasil mengganti nama!");

        } catch (Exception e) {

            pesan("Gagal: " + e.getMessage());
        }
    }

    // Copy file atau folder

    void copyFile() {

        try {

            Path sumber = pathAman(namaFile.getText());
            Path tujuan = pathAman(namaBaru.getText());

            if (!Files.exists(sumber)) {
                throw new IOException("Sumber tidak ditemukan!");
            }

            if (Files.exists(tujuan)) {
                throw new IOException("Tujuan sudah ada!");
            }

            if (Files.isDirectory(sumber)) {

                Files.walkFileTree(
                        sumber,
                        new SimpleFileVisitor<Path>() {

                    public FileVisitResult preVisitDirectory(
                            Path dir,
                            BasicFileAttributes attrs)
                            throws IOException {

                        Files.createDirectories(
                                tujuan.resolve(sumber.relativize(dir))
                        );

                        return FileVisitResult.CONTINUE;
                    }

                    public FileVisitResult visitFile(
                            Path file,
                            BasicFileAttributes attrs)
                            throws IOException {

                        Files.copy(
                                file,
                                tujuan.resolve(sumber.relativize(file))
                        );

                        return FileVisitResult.CONTINUE;
                    }
                });

            } else {

                Files.copy(sumber, tujuan);
            }

            tampilkanFile();

            pesan("Berhasil menyalin!");

        } catch (Exception e) {

            pesan("Gagal: " + e.getMessage());
        }
    }

    // Delete

    void deleteFile() {

        try {

            Path target = pathAman(namaFile.getText());

            if (!Files.exists(target)) {
                throw new IOException("File tidak ditemukan!");
            }

            int konfirmasi = JOptionPane.showConfirmDialog(
                    this,
                    "Yakin ingin menghapus?",
                    "Konfirmasi",
                    JOptionPane.YES_NO_OPTION
            );

            if (konfirmasi != JOptionPane.YES_OPTION) {
                return;
            }

            Files.walkFileTree(
                    target,
                    new SimpleFileVisitor<Path>() {

                public FileVisitResult visitFile(
                        Path file,
                        BasicFileAttributes attrs)
                        throws IOException {

                    Files.delete(file);

                    return FileVisitResult.CONTINUE;
                }

                public FileVisitResult postVisitDirectory(
                        Path dir,
                        IOException exc)
                        throws IOException {

                    if (exc != null) throw exc;

                    Files.delete(dir);

                    return FileVisitResult.CONTINUE;
                }
            });

            tampilkanFile();

            pesan("Berhasil menghapus!");

        } catch (Exception e) {

            pesan("Gagal: " + e.getMessage());
        }
    }

    // =================================
    // FITUR 3: PROSES APLIKASI
    // =================================

    JPanel panelProses() {

        JPanel panel = new JPanel(new BorderLayout());

        prosesModel = new DefaultTableModel(
                new String[]{"Nama Aplikasi", "ID", "Status"}, 0
        ) {
            public boolean isCellEditable(int row, int col) {
                return false;
            }
        };

        prosesModel.addRow(
                new Object[]{"Browser", "P001", "Berjalan"}
        );

        prosesModel.addRow(
                new Object[]{"Pemutar Musik", "P002", "Menunggu"}
        );

        prosesModel.addRow(
                new Object[]{"Editor Teks", "P003", "Selesai"}
        );

        JTable tabel = new JTable(prosesModel);

        tabel.setRowHeight(30);

        JButton mulai = new JButton("Mulai Proses");
        JButton berhenti = new JButton("Hentikan Proses");

        JPanel tombol = new JPanel();

        tombol.add(mulai);
        tombol.add(berhenti);

        mulai.addActionListener(e ->
                ubahStatus(tabel, "Berjalan"));

        berhenti.addActionListener(e ->
                ubahStatus(tabel, "Dihentikan"));

        panel.add(new JScrollPane(tabel), BorderLayout.CENTER);
        panel.add(tombol, BorderLayout.SOUTH);

        return panel;
    }

    void ubahStatus(JTable tabel, String status) {

        int baris = tabel.getSelectedRow();

        if (baris == -1) {

            pesan("Pilih aplikasi terlebih dahulu!");

        } else {

            prosesModel.setValueAt(status, baris, 2);
        }
    }

    // =================================
    // PESAN DAN MAIN
    // =================================

    void pesan(String teks) {

        JOptionPane.showMessageDialog(this, teks);
    }

    public static void main(String[] args) {

        SwingUtilities.invokeLater(() -> {

            SimulasiSistemKomputer app =
                    new SimulasiSistemKomputer();

            app.setVisible(true);
        });
    }
}
