package org.superbiz.moviefun;

import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectInputStream;
import org.superbiz.moviefun.blobstore.Blob;
import org.superbiz.moviefun.blobstore.BlobStore;

import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;

public class S3Store implements BlobStore {

    AmazonS3Client client;
    String storageBucket;

    public S3Store(AmazonS3Client s3Client, String photoStorageBucket) {

        this.client = s3Client;
        this.storageBucket = photoStorageBucket;
    }

    @Override
    public void put(Blob blob) throws IOException {


       ObjectMetadata myMetaData = new ObjectMetadata();
       myMetaData.setContentType( blob.contentType);

       client.putObject(storageBucket, blob.getName(), blob.getInputStream(), myMetaData);
    }

    @Override
    public Optional<Blob> get(String name) throws IOException {


        try{
            System.err.println("String filename:" + name);
             if (client.doesObjectExist(storageBucket, name)){
                 System.err.println("isnotnull");
                 S3Object returnObject = client.getObject(storageBucket, name);

                 System.err.println("filesize:" + returnObject.getObjectContent().available());
                 InputStream s3inputStream = returnObject.getObjectContent();


                 Blob returnBlob= new Blob(name, s3inputStream, returnObject.getObjectMetadata().getContentType());
                 return Optional.of(returnBlob);

             }
            return Optional.empty();
        }catch(Exception ex){
            return Optional.empty();
        }
    }

    @Override
    public void deleteAll() {

    }
}
