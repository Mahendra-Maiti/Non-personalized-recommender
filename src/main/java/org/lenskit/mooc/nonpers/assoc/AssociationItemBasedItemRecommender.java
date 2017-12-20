package org.lenskit.mooc.nonpers.assoc;

import it.unimi.dsi.fastutil.longs.LongSet;
import org.lenskit.api.Result;
import org.lenskit.api.ResultList;
import org.lenskit.basic.AbstractItemBasedItemRecommender;
import org.lenskit.results.Results;
import org.lenskit.util.collections.LongUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import javax.inject.Inject;
import java.util.*;


/**
 * An item-based item scorer that uses association rules.
 */

public class AssociationItemBasedItemRecommender extends AbstractItemBasedItemRecommender {
    private static final Logger logger = LoggerFactory.getLogger(AssociationItemBasedItemRecommender.class);
    private final AssociationModel model;

    /**
     * Construct the item scorer.
     *
     * @param m The association rule model.
     */
    @Inject
    public AssociationItemBasedItemRecommender(AssociationModel m) {
        model = m;
    }

    @Override
    public ResultList recommendRelatedItemsWithDetails(Set<Long> basket, int n, @Nullable Set<Long> candidates, @Nullable Set<Long> exclude) {
        LongSet items;
        if (candidates == null) {
            items = model.getKnownItems();
        } else {
            items = LongUtils.asLongSet(candidates);
        }

        if (exclude != null) {
            items = LongUtils.setDifference(items, LongUtils.asLongSet(exclude));
        }

        if (basket.isEmpty()) {
            return Results.newResultList();
        } else if (basket.size() > 1) {
            logger.warn("Reference set has more than 1 item, picking arbitrarily.");
        }

        long refItem = basket.iterator().next();

        return recommendItems(n, refItem, items);
    }

    public static Comparator<Result> res=new Comparator<Result>() {
        @Override
        public int compare(Result rate1, Result rate2) {
            double first_rating=rate1.getScore();
            double second_rating=rate2.getScore();
            if(first_rating>second_rating){
                return -1;
            }
            else if(second_rating>first_rating){
                return 1;
            }
            else{
                return 0;
            }

        }
    };

    /**
     * Recommend items with an association rule.
     * @param n The number of recommendations to produce.
     * @param refItem The reference item.
     * @param candidates The candidate items (set of items that can possibly be recommended).
     * @return The list of results.
     */
    private ResultList recommendItems(int n, long refItem, LongSet candidates) {
        List<Result> results = new ArrayList<>();
        Iterator it=candidates.iterator();
        while(it.hasNext()){
            long movie_id=(long)it.next();
            if(model.hasItem(movie_id)){
                double value=model.getItemAssociation(refItem,movie_id);
                results.add(Results.create(movie_id,value));
            }
            results.sort(res);

        }
        return Results.newResultList(results.subList(0,n));
    }
}