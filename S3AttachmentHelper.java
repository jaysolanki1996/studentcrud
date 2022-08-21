package my.airo.roboadvisor.infra.helper;

import java.io.File;
import java.io.InputStream;

import javax.annotation.PostConstruct;

import org.apache.log4j.Logger;
import org.jets3t.service.S3Service;
import org.jets3t.service.acl.AccessControlList;
import org.jets3t.service.impl.rest.httpclient.RestS3Service;
import org.jets3t.service.model.S3Bucket;
import org.jets3t.service.model.S3Object;
import org.jets3t.service.security.AWSCredentials;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import my.airo.roboadvisor.infra.exception.SystemException;
import my.airo.roboadvisor.infra.model.IAttachmentable;
import my.airo.roboadvisor.infra.service.AsynchronousTask;

@Component
public class S3AttachmentHelper extends AbstractHelper implements IAttachmentableHelper {

    private static final String LOCAL_BUCKET = "local";

    private static String S3_BUCKET_PREFIX;

    protected Logger logger = Logger.getLogger(S3AttachmentHelper.class);

    // s3 stuff
    private AWSCredentials awsCredentials;

    private S3Service s3Service;

    private S3Bucket s3bucket;

    private AccessControlList bucketAcl;

    @PostConstruct
    protected void init() throws Exception {

        S3_BUCKET_PREFIX = propertiesHelper.appNameStylizedShort + "/uploads/";

        if (!propertiesHelper.awsBucketName.equals(LOCAL_BUCKET)) {
            connectToS3();
        }
    }

    private void connectToS3() {
        new AsynchronousTask(userOperationContextService) {
            @Override
            public void body() throws Exception {
                try {
                    awsCredentials = new AWSCredentials(propertiesHelper.awsAccesskey, propertiesHelper.awsSecretkey);
                    s3Service = new RestS3Service(awsCredentials);

                    s3bucket = s3Service.getBucket(propertiesHelper.awsBucketName);
                    if (s3bucket == null) {
                        logger.info("unable to find bucket :" + propertiesHelper.awsBucketName);
                    } else {
                        logger.info("connected to s3bucket: " + propertiesHelper.awsBucketName);
                    }

                    // make bucket public
                    bucketAcl = s3Service.getBucketAcl(s3bucket);
                    s3bucket.setAcl(bucketAcl);
                    s3Service.putBucketAcl(s3bucket);
                } catch (Exception e) {
                    userOperationContextService.warn(e);
                }
            }
        }.execute();
    }

    @Override
    public String saveFile(final IAttachmentable attachment) {
        try {

            S3Object fileObject = new S3Object(S3_BUCKET_PREFIX + attachment.getFilePath(attachment.getAttachment().getContentType()) + "/" + attachment.getAttachmentName() + "." + getFormatNameFromContentType(attachment.getAttachment().getContentType()));
            fileObject.setDataInputStream(attachment.getAttachment().getInputStream());
            fileObject.setContentType(attachment.getAttachment().getContentType());
            fileObject.setContentLength(attachment.getAttachment().getBytes().length);
            s3Service.putObject(s3bucket, fileObject);
        } catch (Exception e) {
        	logger.error(e.getMessage(), e);
        }
        return attachment.getId();
    }

    /**
     * Used for save one file
     * @param attachmentFile
     * @param filePath
     * @param fileName
     * @return
     */
    @Override
    public String saveFile(final MultipartFile attachmentFile, String filePath, String fileName) {
        try {

            S3Object fileObject = new S3Object(S3_BUCKET_PREFIX + filePath + "/" + fileName);
            fileObject.setDataInputStream(attachmentFile.getInputStream());
            fileObject.setContentType(attachmentFile.getContentType());
            fileObject.setContentLength(attachmentFile.getBytes().length);
            s3Service.putObject(s3bucket, fileObject);
        } catch (Exception e) {
        	logger.error(e.getMessage(), e);
        }
        return fileName;
    }

    @Override
    public String getFilename(String id) {
        String filename = getFileNameHash(id);
        return getS3Filename(id, filename);
    }

    private String getS3Filename(String id, String filename) {
        return id;
    }

    @Override
    public InputStream retrieveFileInputStream(String id) throws SystemException {
        try {

            S3Object o = s3Service.getObject(propertiesHelper.awsBucketName, id);
            if (o != null) {
                return o.getDataInputStream();
            }
        } catch (Exception e) {
           logger.error(e.getMessage(), e);
        }
        return null;
    }

    @Override
    public Object[] retrieveFileInputStreamAndContentType(String id) throws SystemException {
        String filename = getFileNameHash(id);
        try {

            S3Object o = s3Service.getObject(propertiesHelper.awsBucketName, id);
            if (o != null) {
                return new Object[] { o.getDataInputStream(), o.getContentType() };
            }
            return null;
        } catch (Exception e) {
            throw new SystemException(e);
        }
    }

	@Override
	public boolean deleteFile(final String id) {
		try {
			s3Service.deleteObject(s3bucket, id);

		} catch (Exception e) {
			logger.warn("S3ServiceException: ResponseCode:403, can ignore this exception.");
		}
		return true;
	}

    @Override
    public boolean deleteFile(final String filePath, final String fileName) {
        try {
            // final String filename = getFileNameHash(id);

            String fileAbsolutePath = S3_BUCKET_PREFIX + filePath + "/" + fileName;
            deleteFile(fileAbsolutePath);
        } catch (Exception e) {
        	logger.warn("S3ServiceException: ResponseCode:403, can ignore this exception.");
        }
        return true;
    }

    @Override
    public String getFileNameHash(String original) {
        return original;
    }

    public String getUploadsPath() throws SystemException {
        String path = propertiesHelper.appDeploy + System.getProperty("file.separator") + S3_BUCKET_PREFIX + System.getProperty("file.separator");
        File file = new File(path);
        if (!file.exists()) {
            try {
                file.mkdirs();
            } catch (Exception e) {
                throw new SystemException(e);
            }
        }
        return path;
    }

    @Override
    public InputStream retrieveFileInputStream(IAttachmentable iAttachmentable, String extension) throws SystemException {
        String filePath = "";
        filePath = S3_BUCKET_PREFIX + iAttachmentable.getFilePath() + "/" + iAttachmentable.getAttachmentName() + "." + extension;
        return retrieveFileInputStream(filePath);
    }

    @Override
    public InputStream retrieveFileInputStream(String filepath, String fileName) throws SystemException {
        String fileAbsolutePath = S3_BUCKET_PREFIX + filepath + "/" + fileName;
        return retrieveFileInputStream(fileAbsolutePath);
    }

    public String getS3BucketName() {
        return propertiesHelper.awsBucketName;
    }

    @Override
    public String getFormatNameFromContentType(String contentType) {
        contentType = contentType.toLowerCase();
        if (contentType.contains("png")) {
            return "png";
        } else if (contentType.contains("jpeg")) {
            return "jpeg";
        } else if (contentType.contains("jpg")) {
            return "jpg";
        } else if (contentType.contains("gif")) {
            return "gif";
        } else if (contentType.contains("pdf")) {
            return "pdf";
        }
        return "jpg"; // default to jpg
    }

	@Override
	public String getDefaultPath() {
		return S3_BUCKET_PREFIX;
	}

}
