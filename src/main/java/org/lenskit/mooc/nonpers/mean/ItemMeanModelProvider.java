package org.lenskit.mooc.nonpers.mean;

import it.unimi.dsi.fastutil.longs.Long2DoubleOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2IntOpenHashMap;
import org.lenskit.data.dao.DataAccessObject;
import org.lenskit.data.ratings.Rating;
import org.lenskit.inject.Transient;
import org.lenskit.util.io.ObjectStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Provider;

import java.util.Iterator;
import java.util.Map;


/**
 * Provider class that builds the mean rating item scorer, computing item means from the
 * ratings in the DAO.
 */
public class ItemMeanModelProvider implements Provider<ItemMeanModel> {
    
    private static final Logger logger = LoggerFactory.getLogger(ItemMeanModelProvider.class);

    /**
     * The data access object, to be used when computing the mean ratings.
     */
    private final DataAccessObject dao;

    /**
     * Constructor for the mean item score provider.
     *
     * <p>The {@code @Inject} annotation tells LensKit to use this constructor.
     *
     * @param dao The data access object (DAO), where the builder will get ratings.  The {@code @Transient}
     *            annotation on this parameter means that the DAO will be used to build the model, but the
     *            model will <strong>not</strong> retain a reference to the DAO.  This is standard procedure
     *            for LensKit models.
     */
    @Inject
    public ItemMeanModelProvider(@Transient DataAccessObject dao) {
        this.dao = dao;
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

        Long2DoubleOpenHashMap  movie_id_rating=new Long2DoubleOpenHashMap();
        Long2DoubleOpenHashMap number_of_users=new Long2DoubleOpenHashMap();

        try (ObjectStream<Rating> ratings = dao.query(Rating.class).stream()) {
            for (Rating r: ratings) {
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
        Long2DoubleOpenHashMap mean_values = new Long2DoubleOpenHashMap();
        Iterator it=movie_id_rating.keySet().iterator();
        while(it.hasNext()){
            long key= (Long) it.next();
            mean_values.put(key,movie_id_rating.get(key)/number_of_users.get(key));
        }
        logger.debug(mean_values.toString());
        logger.info("computed mean ratings for {} items", mean_values.size());
        return new ItemMeanModel(mean_values);
    }
}
