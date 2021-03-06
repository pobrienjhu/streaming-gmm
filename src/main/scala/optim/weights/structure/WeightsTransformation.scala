package com.github.gradientgmm.optim

import breeze.linalg.{DenseMatrix => BDM, DenseVector => BDV, Vector => BV}

/**
  * Contains the basic functionality for any mapping to be used to 
  * fit the weights of a mixture model

  */
trait WeightsTransformation extends Serializable {


/**
  * Take a valid weight vector of a mixture model and map it to
  * another in which unconstrained gradient ascent can be performed.
  * see [[https://en.wikipedia.org/wiki/Simplex]]
  *
  * @param weights mixture weights
  */

	def fromSimplex(weights: BDV[Double]): BDV[Double]

/**
  * Take an arbitrary vector and map it to the weight simplex
  * see [[https://en.wikipedia.org/wiki/Simplex]]
  *
  * @param real vector
  * @return valid mixture weight vector
  */
	def toSimplex(soft: BDV[Double]): BDV[Double]

/**
  * Prevents underflows and overflows when optimizing the weights

  * Because of the functions involved in mapping points from and to the weight simplex
  * sometimes over/underflows can occur and make some weights go to zero or one.
  * To avoid this, at each iteration this function add an offset to the vector without affecting
  * the resulting simplex points, or bound them if necessary, in this case affecting the resulting
  * weights.

  * @param soft (possibly) arbitrary vector that is going to be mapped to a valid weight vector 
  * @return rescaled/translated vector
  */
	def bound(soft: BDV[Double]): BDV[Double]
}