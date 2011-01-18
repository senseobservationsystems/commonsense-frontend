package nl.sense_os.commonsense.server;

import com.google.appengine.api.blobstore.BlobKey;
import com.google.appengine.api.blobstore.BlobstoreService;
import com.google.appengine.api.blobstore.BlobstoreServiceFactory;
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

import nl.sense_os.commonsense.server.dao.FloorDao;
import nl.sense_os.commonsense.shared.building.FloorModel;

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
public class FloorImgServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private static final Logger log = Logger.getLogger(FloorImgServlet.class.getName());
    private BlobstoreService blobstoreService = BlobstoreServiceFactory.getBlobstoreService();

    @Override
    public void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException,
            IOException {

        Map<String, BlobKey> blobs = blobstoreService.getUploadedBlobs(req);
        BlobKey blobKey = blobs.get("image");

        if (blobKey == null) {
            // Uh ... something went really wrong here
            log.severe("No image in POST");
        } else {
            log.info("Storing floor...");

            ImagesService imagesService = ImagesServiceFactory.getImagesService();

            // Get the properties of the Floor and Building
            String imageUrl = imagesService.getServingUrl(blobKey);
            String name = req.getParameter("label");
            int number = Integer.parseInt(req.getParameter("nr"));
            double height = Double.parseDouble(req.getParameter("height"));
            double width = Double.parseDouble(req.getParameter("width"));
            double depth = Double.parseDouble(req.getParameter("depth"));
            String userId = req.getParameter("user");
            Date now = new Date();

            // store the floor
            FloorModel floor = new FloorModel(imageUrl, number, name, height, width, depth, userId,
                    now, now);
            floor.setBlobKey(blobKey.getKeyString());
            String floorKey = new FloorDao().store(floor);
            floor.setKey(floorKey);

            log.info("Floor \"" + floor.getName() + "\" stored.");

            // Redirect to this servlet's doGet, pass the image url and floor key
            res.sendRedirect("/floorupload?imageUrl=" + imageUrl + "&floorKey=" + floor.getKey());
        }
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException,
            IOException {

        String imageUrl = req.getParameter("imageUrl");
        String floorKey = req.getParameter("floorKey");
        resp.setHeader("Content-Type", "text/html");

        // This is a bit hacky, but it'll work. We'll use this key in an Async service to fetch the
        // image and image information
        resp.getWriter().println("?url=" + imageUrl + "&key=" + floorKey);
    }
}
