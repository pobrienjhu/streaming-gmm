import org.scalatest.FlatSpec


import com.github.gradientgmm.optim.algorithms.GradientAscent
import com.github.gradientgmm.model.GradientAggregator
import com.github.gradientgmm.components.UpdatableGaussianComponent

import breeze.linalg.{diag, eigSym, max, DenseMatrix => BDM, DenseVector => BDV, Vector => BV, trace, norm}

/**
  * This small test creates a Mixture model with two components 
  * whose weights are 0.5,0.5 and are centered at (1,0), (-1,0)
  * with identity covariance matrices.
  * We generate several 'sample' points equal to (0,1) and test the gradient and log-likelihood calulcations.
  * this toy case is easy to analyze and the correct results were derived with pen and paper.
  */
class AggregatorTest extends FlatSpec{
	
	
	var dim = 2
	var nPoints = 5
	var errorTol = 1e-8
	
	val clusterMeans = Array(new BDV(Array(-1.0,0.0)), new BDV(Array(1.0,0.0)))
	val clusterWeights = Array(0.5,0.5)
	val clusterVars = Array.fill(2)(BDM.eye[Double](dim))

	//val mixture = GradientBasedGaussianMixture(clusterWeights,clusterDists)
	val clusterDists = clusterMeans.zip(clusterVars).map{ case(m,v) => UpdatableGaussianComponent(m,v)}

	val optim = new GradientAscent()
		.setLearningRate(0.5)
		.setShrinkageRate(1.0)

	val targetPoint = new BDV(Array(0.0,1.0))
	// do y = [x 1]
	val points = Array.fill(nPoints)(targetPoint).map{case v => new BDV(v.toArray ++ Array(1.0))}


	val adder = GradientAggregator.add(clusterWeights, clusterDists, optim, nPoints)_
	
	val agg = points.foldLeft(GradientAggregator.init(2,dim)){case (agg,point) => adder(agg,point)}

	"the counter" should "be equal to nPoints" in {
		//weightsGradient should be zero
		assert(agg.counter == nPoints)
	}

	"the log-likelihood" should "be correclty calculated" in {
		val correctValue = (-1.0 -math.log(2*math.Pi))

		var error = math.pow(agg.loss - correctValue,2)
		assert(error < errorTol)
	}

	"the posterior membership probabilities" should "be correclty calculated" in {
		//weightsGradient should be zero
		assert(norm(agg.weightsGradient) < errorTol)
	}

	"the descent direction" should "be correclty calculated" in {
		val correctValue = {
			val v = new BDV(targetPoint.toArray ++ Array(1.0))

			clusterDists.map{ case d => (v*v.t - d.paramMat) * 0.25}
		}

		var error = correctValue.zip(agg.gaussianGradients).map{case (a,b) => trace((a-b).t*(a-b))}.sum
		assert(error < errorTol)
	}
}