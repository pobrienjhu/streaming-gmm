package streamingGmm

import breeze.linalg.{DenseMatrix => BDM, DenseVector => BDV, Vector => BV}

class GMMMomentumGradientAscent(
	learningRate: Double,
	regularizer: Option[GMMRegularizer],
	var decayRate: Double) extends GMMGradientAscent(learningRate,regularizer) {

	def setDecayRate(decayRate: Double): Unit = { 
		require(decayRate > 0 , "decay rate must be positive")
		this.decayRate = decayRate
	}

	def getDecayRate: Double = { 
		this.decayRate
	}

	override def direction(dist: UpdatableMultivariateGaussian, sampleInfo: BDM[Double]): BDM[Double] = {

		if(!dist.momentum.isDefined){
			dist.initializeMomentum
		}

		val grad = lossGradient(dist, sampleInfo)
		
		dist.updateMomentum(dist.momentum.get*decayRate + grad)

		dist.momentum.get
	}

	override def softWeightsDirection(posteriors: BDV[Double], weights: SGDWeights): BDV[Double] = {

		if(!weights.momentum.isDefined){
			weights.initializeMomentum
		}

		val grad = softWeightGradient(posteriors, new BDV(weights.weights))
		
		weights.updateMomentum(weights.momentum.get*decayRate + grad)

		weights.momentum.get

	}

}