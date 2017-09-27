package edu.unh.cs.domain

data class SuccessorBundle<State, Action>(val successor: State, val action: Action, val actionCost: Double)