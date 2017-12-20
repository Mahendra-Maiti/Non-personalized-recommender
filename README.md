# Non-Personalized Recommender

In this assignment, some non-personalized recommenders are implemented.  In particular, they are: raw and damped item mean recommenders, simple and advanced association rule recommenders.

## Input Data

The input data contains the following files:

- `ratings.csv` contains user ratings of movies
- `movies.csv` contains movie titles
- `movielens.yml` is a LensKit data manifest that describes the other input files

## Mean-Based Recommendation

The first two recommenders recommend items with the highest average rating.

With LensKit's scorer-model-builder architecture, the recommendation logic is required to be written only once. Two different mechanisms for computing item mean ratings can be used with the same logic.

For mean-based recommendation the classes are :

-   `MeanItemBasedItemRecommender` (the *item recommender*) computes top-*N* recommendations based
    on mean ratings. 

-   `ItemMeanModel` is a *model class* that stores precomputed item means. 

-   `ItemMeanModelProvider` computes item mean ratings from rating data and constructs the model.
    It computes raw means with no damping.

-   `DampedItemMeanModelProvider` is an alternate builder for item mean models that computes
    damped means instead of raw means.  It takes the damping term as a parameter. 

### Computing Item Means

 The `ItemMeanModelProvider` class computes the mean rating for each item.

### Recommending Items

The `MeanItemBasedItemRecommender` class computes recommendations based on item mean
ratings. Recommendation is done as per the following steps:

1.  Obtain the mean rating for each item
2.  Order the items in decreasing order
3.  Return the *N* highest-rated items

### Computing Damped Item Means

The `DampedItemMeanModelProvider` class is used to compute the damped mean rating for each item.
This formula uses a damping factor ![equation](http://latex.codecogs.com/gif.latex?$\alpha$), which is the number of 'fake' ratings at the global
mean to assume for each item.  In the Java code, this is available as the field `damping`.

The damped mean formula, is:

![equation](http://latex.codecogs.com/gif.latex?$$s(i)&space;=&space;\frac{\sum_{u&space;\in&space;U_i}&space;r_{ui}&space;&plus;&space;\alpha\mu}{|U_i|&space;&plus;&space;\alpha}$$)

where ![equation](http://latex.codecogs.com/gif.latex?$\mu$) is the *global* mean rating.

## Association Rules

The association rule implementation consists of the following code:

-   `AssociationItemBasedItemRecommender` recommends items using association rules.  Unlike the mean
    recommenders, this recommender uses a *reference item* to compute the recommendations.
-   `AssociationModel` stores the association rule scores between pairs of items.  You will not need
    to modify this class.
-   `BasicAssociationModelProvider` computes an association rule model using the basic association
    rule formula ![equation](http://latex.codecogs.com/gif.latex?($P(X&space;\wedge&space;Y)&space;/&space;P(X)$)).
-   `LiftAssociationModelProvider` computes an association rule model using the lift formula ![equation](http://latex.codecogs.com/gif.latex?($P(X&space;\wedge&space;Y)&space;/&space;P(X)&space;P(Y)$)).

### Computing Association Scores

Like with the mean-based recommender, the product association scores are pre-computed and stored in
a model before recommendation. The scores between *all pairs* of items are computed, so that the
model can be used to score any item.  When computing a single recommendation from the command line,
this does not provide much benefit, but is useful in the general case so that the model can be used
to very quickly compute many recommendations.

The `BasicAssociationModelProvider` class computes the association rule scores using the following
formula:

![equation](https://latex.codecogs.com/gif.latex?%24%24P%28i%7Cj%29%20%3D%20%5Cfrac%7BP%28i%20%5Cwedge%20j%29%7D%7BP%28j%29%29%7D%20%3D%20%5Cfrac%7B%7CU_i%20%5Ccap%20U_j%7C/%7CU%7C%7D%7B%7CU_j%7C/%7CU%7C%7D%24%24)

In this case, ![equation](http://latex.codecogs.com/gif.latex?$j$) is the *reference* item and ![equation](http://latex.codecogs.com/gif.latex?$i$) is the item to be scored.

The probabilities are estimated by counting: ![equation](http://latex.codecogs.com/gif.latex?$P(i)$) is the fraction of users in the system
who purchased item ![equation](http://latex.codecogs.com/gif.latex?$i$);![equation](http://latex.codecogs.com/gif.latex?$P(i&space;\wedge&space;j)$)  is the fraction that purchased both ![equation](http://latex.codecogs.com/gif.latex?$i$) and ![equation](http://latex.codecogs.com/gif.latex?$j$).


### Computing Recommendations

The recommendation logic in `AssociationItemBasedItemRecommender` is used to recommend items
related to a given reference item.  As with the mean recommender, it computes the top *N*
recommendations and return them.


### Computing Advanced Association Rules

The `LiftAssociationModelProvider` recommender uses the *lift* metric that computes how
much more likely someone is to rate a movie ![equation](http://latex.codecogs.com/gif.latex?$i$)  when they have rated ![equation](http://latex.codecogs.com/gif.latex?$j$) than they would have if we do not know anything about whether they have rated ![equation](http://latex.codecogs.com/gif.latex?$j$):

![equation](http://latex.codecogs.com/gif.latex?$$s(i|j)&space;=&space;\frac{P(j&space;\wedge&space;i)}{P(i)&space;P(j)}$$)

## Running the code

The following Gradle targets will do this:

- `runMean` runs the raw mean recommender
- `runDampedMean` runs the damped mean recommender
- `runBasicAssoc` runs the basic association rule recommender
- `runLiftAssoc` runs the advanced (lift-based) association rule recommender

These can be run using the IntelliJ Gradle runner (open the Gradle panel, browse the tree to find
a task, and double-click it), or from the command line:

    ./gradlew runMean

The association rule recommenders can also take the reference item ID on the command line as a
`referenceItem` parameter.  For example:

    ./gradlew runLiftAssoc -PreferenceItem=1

