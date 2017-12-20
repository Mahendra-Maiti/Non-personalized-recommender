package org.lenskit.mooc.nonpers.mean;

import it.unimi.dsi.fastutil.longs.Long2DoubleOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2IntOpenHashMap;
import org.lenskit.baseline.MeanDamping;
import org.lenskit.data.dao.DataAccessObject;
import org.lenskit.data.ratings.Rating;
import org.lenskit.inject.Transient;
import org.lenskit.util.io.ObjectStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Provider;
import java.util.Iterator;


/**
 * Provider class that builds the mean rating item scorer, computing damped item means from the
 * ratings in the DAO.
 */
public class DampedItemMeanModelProvider implements Provider<ItemMeanModel> {
    /**
     * A logger that you can use to emit debug messages.
     */
    private static final Logger logger = LoggerFactory.getLogger(DampedItemMeanModelProvider.class);

    /**
     * The data access object, to be used when computing the mean ratings.
     */
    private final DataAccessObject dao;
    /**
     * The damping factor.
     */
    private final double damping;

    /**
     * Constructor for the mean item score provider.
     *
     * <p>The {@code @Inject} annotation tells LensKit to use this constructor.
     *
     * @param dao The data access object (DAO), where the builder will get ratings.  The {@code @Transient}
     *            annotation on this parameter means that the DAO will be used to build the model, but the
     *            model will <strong>not</strong> retain a reference to the DAO.  This is standard procedure
     *            for LensKit models.
     * @param damping The damping factor for Bayesian damping.  This is number of fake global-mean ratings to
     *                assume.  It is provided as a parameter so that it can be reconfigured.  See the file
     *                {@code damped-mean.groovy} for how it is used.
     */
    @Inject
    public DampedItemMeanModelProvider(@Transient DataAccessObject dao,
                                       @MeanDamping double damping) {
        this.dao = dao;
        this.damping = damping;
    }

    /**
     * Construct an item mean model.
     *
     * <p>The {@link Provider#get()} method constructs whatever object the provider class is intended to build.</p>
     *
     * @return The item mean model with mean ratings for all items.
     */
    @Override
    public ItemMeanModel get() {

        logger.debug("Computing damped mean for all the movies");
        logger.info("Computing damped mean for all the movies");


        Long2DoubleOpenHashMap  movie_id_rating=new Long2DoubleOpenHashMap();
        Long2DoubleOpenHashMap number_of_users=new Long2DoubleOpenHashMap();

        double global_mean;
        double global_sum=0;
        double global_count=0;

        try (ObjectStream<Rating> ratings = dao.query(Rating.class).stream()) {
            logger.debug("Processing Ratings");
            for (Rating r : ratings) {

                global_sum+=r.getValue();
                global_count++;
                if (!movie_id_rating.containsKey(r.getItemId())) {
                    movie_id_rating.put(r.getItemId(), r.getValue());
                } else {
                    movie_id_rating.put(r.getItemId(), movie_id_rating.get(r.getItemId()) + r.getValue());
                }
                if (!number_of_users.containsKey(r.getItemId())) {
                    number_of_users.put(r.getItemId(),1 );
                } else {
                    number_of_users.put(r.getItemId(), number_of_users.get(r.getItemId()) + 1);
                }
            }
        }
        global_mean=global_sum/global_count;
        Long2DoubleOpenHashMap means = new Long2DoubleOpenHashMap();
        Iterator it=movie_id_rating.keySet().iterator();
        while(it.hasNext()){
            long key= (Long) it.next();
            means.put(key,(movie_id_rating.get(key)+global_mean*damping)/(number_of_users.get(key)+damping));
        }
        logger.debug(means.toString());
        logger.info("computed damped mean ratings for {} items", means.size());
        return new ItemMeanModel(means);
    }
}
