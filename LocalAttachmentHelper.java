package my.airo.roboadvisor.infra.helper;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

import org.springframework.web.multipart.MultipartFile;

import my.airo.roboadvisor.infra.exception.SystemException;
import my.airo.roboadvisor.infra.model.IAttachmentable;

//@Component
public class LocalAttachmentHelper extends AbstractHelper implements IAttachmentableHelper {

    private static final String LOCAL_BUCKET_FOLDER_NAME = "uploads";

    @Override
    public String saveFile(IAttachmentable attachment) {
        try {

            String filename = attachment.getId();
            File fileToCreate = new File(getUploadsPath() + filename);

            if (!fileToCreate.exists()) {
                fileToCreate.createNewFile();
            } else {
                fileToCreate.delete();
            }

            attachment.getAttachment().transferTo(fileToCreate);

        } catch (Exception e) {
            throw new SystemException(e);
        }

        return attachment.getId();
    }

    @Override
    public String getFilename(String id) {
        String filename = getFileNameHash(id);
        return getUploadsPath() + filename;
    }

    @Override
    public InputStream retrieveFileInputStream(String id) {
        try {
            return new FileInputStream(new File(getFilename(id)));

        } catch (Exception e) {
            throw new SystemException(e);
        }

    }

    @Override
    public Object[] retrieveFileInputStreamAndContentType(String id) {
        String filename = getFileNameHash(id);
        try {
            File f = new File(getUploadsPath() + filename);
            if (f.exists()) {
                return new Object[] {new FileInputStream(f), "image/jpeg"}; // @TODO: don't hard
                                                                            // code this
            }
            return null;

        } catch (Exception e) {
            throw new SystemException(e);
        }

    }

    @Override
    public boolean deleteFile(String id) {
        final String filename = getFileNameHash(id);
        File fileToDelete = new File(getUploadsPath() + filename);

        if (fileToDelete.exists()) {
            fileToDelete.delete();
        }

        return true;
    }

    @Override
    public String getFileNameHash(String original) {
        return original;
    }

    public String getUploadsPath() {
        String path = propertiesHelper.appDeploy + System.getProperty("file.separator")
                + LOCAL_BUCKET_FOLDER_NAME + System.getProperty("file.separator");
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
    
    public String getUploadsPath(String filePath) {
        String path = propertiesHelper.appDeploy + System.getProperty("file.separator") + LOCAL_BUCKET_FOLDER_NAME
                + System.getProperty("file.separator") + filePath;
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
	public InputStream retrieveFileInputStream(IAttachmentable iAttachmentable, String contentType) {
		String filePath = getUploadsPath(iAttachmentable.getFilePath()) + System.getProperty("file.separator")
        + iAttachmentable.getId() + "."
        + getFormatNameFromContentType(iAttachmentable.getAttachment().getContentType());
		
		return retrieveFileInputStream(filePath);
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
	public String saveFile(MultipartFile attachmentFile, String filePath, String fileName) {
        try {
            File fileToCreate = new File(filePath + "/" +fileName);
            if (!fileToCreate.exists()) {
                fileToCreate.createNewFile();
            } else {
                fileToCreate.delete();
            }
            attachmentFile.transferTo(fileToCreate);
        } catch (Exception e) {
//            logger.error("File not uploaded", e);
        }
		return null;
	}

	@Override
	public InputStream retrieveFileInputStream(String filepath, String fileName) {
		return retrieveFileInputStream( filepath +"/"+ fileName);
	}

	@Override
	public boolean deleteFile(String filePath, String fileName) {
		String path = propertiesHelper.appDeploy + System.getProperty("file.separator") + LOCAL_BUCKET_FOLDER_NAME
                + System.getProperty("file.separator") + filePath +System.getProperty("file.separator") +  fileName;
		deleteFile(path);
		return false;
	}

}
