package my.airo.roboadvisor.infra.model;

import org.springframework.web.multipart.MultipartFile;

public interface IAttachmentable {

	public static final String UPLOAD_ALLOWED_EXTENSIONS = ".jpg, .jpeg, .gif, .png";
	
	public String getId();
	
	public MultipartFile getAttachment();

	public void setAttachment(MultipartFile attachment);
	
	public Boolean getImageUploaded();

	public void setImageUploaded(Boolean imageUploaded);
	
	public Boolean getImageToDelete();
	
	public void setImageToDelete(Boolean imageToDelete);
	
	public String getFilePath();
	
	public String getAttachmentName();

}
