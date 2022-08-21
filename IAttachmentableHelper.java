package my.airo.roboadvisor.infra.helper;

import java.io.InputStream;

import org.springframework.web.multipart.MultipartFile;

import my.airo.roboadvisor.infra.exception.SystemException;
import my.airo.roboadvisor.infra.model.IAttachmentable;

public interface IAttachmentableHelper {
	
	public String saveFile(final IAttachmentable attachment) throws SystemException;
	
	public String saveFile(final MultipartFile attachmentFile,String filePath, String fileName) throws SystemException;
	
	public String getFilename(String id);
	
	public InputStream retrieveFileInputStream(String id) throws SystemException;
	
	public Object[] retrieveFileInputStreamAndContentType(String id) throws SystemException;
	
	public boolean deleteFile(final String id) throws SystemException;
	
	public boolean deleteFile(final String filePath, final String fileName) throws SystemException;
	
	public String getFileNameHash(String original);
	
	InputStream retrieveFileInputStream(IAttachmentable iAttachmentable, String contentType) throws SystemException;
	
	public InputStream retrieveFileInputStream(String filepath ,String fileName) throws SystemException;
	
	public String getFormatNameFromContentType(String contentType);
	
	public default String getDefaultPath() {
		return "";
	}

}
