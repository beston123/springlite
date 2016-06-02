package org.springlite.core.io;

import org.springlite.util.Assert;
import org.springlite.util.ResourceUtils;
import org.springlite.util.StringUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.*;

/**
 * 〈一句话功能简述〉&lt;p&gt;
 * 〈功能详细描述〉
 *
 * @author zixiao
 * @date 16/5/28
 * @see [相关类/方法]（可选）
 * @since [产品/模块版本] （可选）
 */
public class UrlResource extends AbstractResource {

    /**
     * Original URL, used for actual access.
     */
    private final URL url;

    /**
     * Cleaned URL (with normalized path), used for comparisons.
     */
    private final URL cleanedUrl;


    /**
     * Create a new UrlResource based on the given URL object.
     * @param url a URL
     */
    public UrlResource(URL url) {
        Assert.notNull(url, "URL must not be null");
        this.url = url;
        this.cleanedUrl = getCleanedUrl(this.url, url.toString());
    }

    /**
     * Create a new UrlResource based on a URL path.
     * <p>Note: The given path needs to be pre-encoded if necessary.
     * @param path a URL path
     * @throws MalformedURLException if the given URL path is not valid
     * @see java.net.URL#URL(String)
     */
    public UrlResource(String path) throws MalformedURLException {
        Assert.notNull(path, "Path must not be null");
        this.url = new URL(path);
        this.cleanedUrl = getCleanedUrl(this.url, path);
    }

    /**
     * Determine a cleaned URL for the given original URL.
     * @param originalUrl the original URL
     * @param originalPath the original URL path
     * @return the cleaned URL
     * @see org.springlite.util.StringUtils#cleanPath
     */
    private URL getCleanedUrl(URL originalUrl, String originalPath) {
        try {
            return new URL(StringUtils.cleanPath(originalPath));
        }
        catch (MalformedURLException ex) {
            // Cleaned URL path cannot be converted to URL
            // -> take original URL.
            return originalUrl;
        }
    }


    /**
     * This implementation opens an InputStream for the given URL.
     * It sets the "UseCaches" flag to {@code false},
     * mainly to avoid jar file locking on Windows.
     * @see java.net.URL#openConnection()
     * @see java.net.URLConnection#setUseCaches(boolean)
     * @see java.net.URLConnection#getInputStream()
     */
    public InputStream getInputStream() throws IOException {
        URLConnection con = this.url.openConnection();
        ResourceUtils.useCachesIfNecessary(con);
        try {
            return con.getInputStream();
        }
        catch (IOException ex) {
            // Close the HTTP connection (if applicable).
            if (con instanceof HttpURLConnection) {
                ((HttpURLConnection) con).disconnect();
            }
            throw ex;
        }
    }

    /**
     * This implementation returns the underlying URL reference.
     */
    @Override
    public URL getURL() throws IOException {
        return this.url;
    }


    /**
     * This implementation returns a File reference for the underlying URL/URI,
     * provided that it refers to a file in the file system.
     * @see org.springlite.util.ResourceUtils#getFile(java.net.URL, String)
     */
    @Override
    public File getFile() throws IOException {
        return new File(this.url.getFile());
    }

    /**
     * This implementation creates a UrlResource, applying the given path
     * relative to the path of the underlying URL of this resource descriptor.
     * @see java.net.URL#URL(java.net.URL, String)
     */
    @Override
    public Resource createRelative(String relativePath) throws MalformedURLException {
        if (relativePath.startsWith("/")) {
            relativePath = relativePath.substring(1);
        }
        return new UrlResource(new URL(this.url, relativePath));
    }

    /**
     * This implementation returns the name of the file that this URL refers to.
     * @see java.net.URL#getFile()
     * @see java.io.File#getName()
     */
    @Override
    public String getFilename() {
        return new File(this.url.getFile()).getName();
    }

    /**
     * This implementation returns a description that includes the URL.
     */
    public String getDescription() {
        return "URL [" + this.url + "]";
    }


    /**
     * This implementation compares the underlying URL references.
     */
    @Override
    public boolean equals(Object obj) {
        return (obj == this ||
                (obj instanceof UrlResource && this.cleanedUrl.equals(((UrlResource) obj).cleanedUrl)));
    }

    /**
     * This implementation returns the hash code of the underlying URL reference.
     */
    @Override
    public int hashCode() {
        return this.cleanedUrl.hashCode();
    }


    @Override
    public boolean isReadable() {
        try {
            URL url = getURL();
            if (ResourceUtils.isFileURL(url)) {
                // Proceed with file system resolution...
                File file = getFile();
                return (file.canRead() && !file.isDirectory());
            }
            else {
                return true;
            }
        }
        catch (IOException ex) {
            return false;
        }
    }


}
