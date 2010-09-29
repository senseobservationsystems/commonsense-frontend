package nl.sense_os.commonsense.server;

import com.google.appengine.api.blobstore.BlobstoreService;
import com.google.appengine.api.blobstore.BlobstoreServiceFactory;
import com.google.appengine.api.users.User;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import com.google.gwt.user.server.rpc.RemoteServiceServlet;

import java.util.List;

import nl.sense_os.commonsense.client.services.UserImageService;
import nl.sense_os.commonsense.dto.UploadedImage;

/**
 * @see <a href="http://ikaisays.com/2010/09/08/gwt-blobstore-the-new-high-performance-image-serving-api-and-cute-dogs-on-office-chairs/">blobstore image service</a>
 */
@SuppressWarnings("serial")
public class UserImageServiceImpl extends RemoteServiceServlet implements UserImageService {

    @Override
    public String getBlobstoreUploadUrl() {
        BlobstoreService blobstoreService = BlobstoreServiceFactory.getBlobstoreService();
        return blobstoreService.createUploadUrl("/upload");
    }

    @Override
    public UploadedImage get(String key) {
        UploadedImageDao dao = new UploadedImageDao();
        UploadedImage image = dao.get(key);
        return image;
    }

    @Override
    public List<UploadedImage> getRecentlyUploaded() {
        UploadedImageDao dao = new UploadedImageDao();
        List<UploadedImage> images = dao.getRecent(); 
        return images;
    }

    @Override
    public void deleteImage(String key) {
        UserService userService = UserServiceFactory.getUserService();
        User user = userService.getCurrentUser();
        UploadedImageDao dao = new UploadedImageDao();
        UploadedImage image = dao.get(key);
//        if(image.getOwnerId().equals(user.getUserId())) {
            dao.delete(key);
//        }
    }
}
