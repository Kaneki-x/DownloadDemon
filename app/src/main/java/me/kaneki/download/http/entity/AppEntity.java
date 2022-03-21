package me.kaneki.download.http.entity;

public class AppEntity {
    private int file_size;
    private String download_link;
    private String icon;
    private String title;
    private String package_name;

    public int getFile_size() { return file_size;}

    public void setFile_size(int file_size) { this.file_size = file_size;}

    public String getDownload_link() { return download_link;}

    public void setDownload_link(String download_link) { this.download_link = download_link;}

    public String getIcon() { return icon;}

    public void setIcon(String icon) { this.icon = icon;}

    public String getTitle() { return title;}

    public void setTitle(String title) { this.title = title;}

    public String getPackage_name() { return package_name;}

    public void setPackage_name(String package_name) { this.package_name = package_name;}
}
