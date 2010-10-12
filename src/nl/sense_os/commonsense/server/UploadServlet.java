package nl.sense_os.commonsense.server;

import com.google.appengine.api.blobstore.BlobKey;
import com.google.appengine.api.blobstore.BlobstoreService;
import com.google.appengine.api.blobstore.BlobstoreServiceFactory;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.images.ImagesService;
import com.google.appengine.api.images.ImagesServiceFactory;

import java.io.IOException;
import java.util.Date;
import java.util.Map;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import nl.sense_os.commonsense.dto.building.Floor;

/**
 * This is the servlet that handles the callback after the blobstore upload has completed. After the
 * blobstore handler completes, it POSTs to the callback URL, which must return a redirect. We
 * redirect to the GET portion of this servlet which sends back a key. GWT needs this Key to make
 * another request to get the image serving URL. This adds an extra request, but the reason we do
 * this is so that GWT has a Key to work with to manage the Image object. Note the content-type. We
 * *need* to set this to get this to work. On the GWT side, we'll take this and show the image that
 * was uploaded.
 * 
 * @author Ikai Lan
 * @see <a
 *      href="http://ikaisays.com/2010/09/08/gwt-blobstore-the-new-high-performance-image-serving-api-and-cute-dogs-on-office-chairs/">blobstore
 *      image service</a>
 */
public class UploadServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    @SuppressWarnings("unused")
    private static final Logger log = Logger.getLogger(UploadServlet.class.getName());
    private BlobstoreService blobstoreService = BlobstoreServiceFactory.getBlobstoreService();

    @Override
    public void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException,
            IOException {

        Map<String, BlobKey> blobs = blobstoreService.getUploadedBlobs(req);
        BlobKey blobKey = blobs.get("image");

        if (blobKey == null) {
            // Uh ... something went really wrong here
        } else {

            ImagesService imagesService = ImagesServiceFactory.getImagesService();

            // Get the image serving URL
            String imageUrl = imagesService.getServingUrl(blobKey);

            // For the sake of clarity, we'll use low-level entities
            Entity uploadedImage = new Entity("UploadedImage");
            uploadedImage.setProperty("blobKey", blobKey);
            uploadedImage.setProperty(Floor.CREATED_AT, new Date());
            uploadedImage.setProperty(Floor.OWNER_ID, req.getParameter("owner"));
            
            // Highly unlikely we'll ever filter on this property
            uploadedImage.setUnindexedProperty(Floor.SERVING_URL, imageUrl);

            DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
            datastore.put(uploadedImage);

            res.sendRedirect("/upload?imageUrl=" + imageUrl);
        }
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException,
            IOException {

        String imageUrl = req.getParameter("imageUrl");
        resp.setHeader("Content-Type", "text/html");

        // This is a bit hacky, but it'll work. We'll use this key in an Async service to fetch the
        // image and image information
        resp.getWriter().println(imageUrl);
    }
}
