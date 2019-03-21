package org.superbiz.moviefun.albums;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.superbiz.moviefun.S3Store;
import org.superbiz.moviefun.blobstore.Blob;
import org.superbiz.moviefun.blobstore.BlobStore;

import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Optional;

import static java.lang.String.format;

@Controller
@RequestMapping("/albums")
public class AlbumsController {

    private final AlbumsBean albumsBean;
    private BlobStore blobStore;

    public AlbumsController(AlbumsBean albumsBean, BlobStore blobStore) {
        this.albumsBean = albumsBean;
        this.blobStore = blobStore;
    }


    @GetMapping
    public String index(Map<String, Object> model) {
        model.put("albums", albumsBean.getAlbums());
        return "albums";
    }

    @GetMapping("/{albumId}")
    public String details(@PathVariable long albumId, Map<String, Object> model) {
        model.put("album", albumsBean.find(albumId));
        return "albumDetails";
    }

    @PostMapping("/{albumId}/cover")
    public String uploadCover(@PathVariable long albumId, @RequestParam("file") MultipartFile uploadedFile) throws IOException {

        System.err.println("Contenttype:" + uploadedFile.getContentType());
        saveUploadToFile(albumId,     uploadedFile.getContentType(), uploadedFile);

        return format("redirect:/albums/%d", albumId);
    }

    @GetMapping("/{albumId}/cover")
    public HttpEntity<byte[]> getCover(@PathVariable long albumId) throws IOException, URISyntaxException {

        String fileName = getCoverFileName(albumId);

        System.out.println("Filename:" + fileName);

        Optional<Blob> imageBlob= blobStore.get(fileName);


        HttpEntity<byte[]> httpEntity = null;
        byte[] inputFileasArray = null;
        HttpHeaders headers = null;


        if (imageBlob.isPresent()) {
            inputFileasArray =  getByteArrayFromStream(imageBlob.get().inputStream);
            headers = createImageHttpHeaders(imageBlob.get().getContentType(), inputFileasArray);
        } else {
            URI defaultCoverURI = AlbumsController.class.getClassLoader().getResource("default-cover.jpg").toURI();
            inputFileasArray = Files.readAllBytes(Paths.get(defaultCoverURI));
            headers = createImageHttpHeaders("application/octet-stream", inputFileasArray); // decide what to do
        }

        return httpEntity = new HttpEntity<>(inputFileasArray, headers);
    }


    private void saveUploadToFile(Long albumId, String contentType, @RequestParam("file") MultipartFile uploadedFile) throws IOException {
        InputStream inputStream =  new BufferedInputStream( uploadedFile.getInputStream());
        Blob blobToSave = new Blob(getCoverFileName(albumId),  inputStream, contentType);
        blobStore.put(blobToSave);
    }







    private HttpHeaders createImageHttpHeaders(String contentType, byte[] imageBytes) throws IOException {

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType(contentType));
        headers.setContentLength(imageBytes.length);
        return headers;
    }

    //private File getCoverFile(@PathVariable long albumId) {
    //__    String coverFileName = format("covers/%d", albumId);
    //    return new File(coverFileName);
   // }


    private String getCoverFileName(@PathVariable long albumId) {
        String coverFileName = format("covers_%d", albumId);
        return coverFileName;
    }


    private Path getExistingCoverPath(@PathVariable long albumId) throws URISyntaxException {
        File coverFile = null; //getCoverFileName(albumId);

        Path coverFilePath;

        if (coverFile.exists()) {
            coverFilePath = coverFile.toPath();
        } else {
            // THIS APPROACH WILL NOT WORK
            // coverFilePath = Paths.get(getSystemResource("default-cover.jpg").toURI());

            // THIS APPROACH SHOULD WORK
            URI defaultCoverURI = AlbumsController.class.getClassLoader().getResource("default-cover.jpg").toURI();
            coverFilePath = Paths.get(defaultCoverURI);
        }

        return coverFilePath;
    }



    private byte[] getByteArrayFromStream (InputStream is) throws IOException {

        ByteArrayOutputStream buffer = new ByteArrayOutputStream();

        int nRead;
        byte[] data = new byte[16384];

        while ((nRead = is.read(data, 0, data.length)) != -1) {
            buffer.write(data, 0, nRead);
        }

        return buffer.toByteArray();
    }

}
