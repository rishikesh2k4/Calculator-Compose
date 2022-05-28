package com.anafthdev.calcc.ui.main

import androidx.lifecycle.viewModelScope
import com.anafthdev.calcc.data.Calc
import com.anafthdev.calcc.data.CalcCAction
import com.anafthdev.calcc.data.CalcCNumber
import com.anafthdev.calcc.data.CalcCOperation
import com.anafthdev.calcc.foundation.viewmodel.StatefulViewModel
import com.anafthdev.calcc.ui.main.environment.IMainEnvironment
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
	environment: IMainEnvironment
): StatefulViewModel<MainState, Unit, MainAction, IMainEnvironment>(
	MainState(),
	environment
) {
	
	init {
		viewModelScope.launch(environment.dispatcher) {
			environment.getExpression().collect { exp ->
				environment.calculate()
				setState {
					copy(
						expression = exp
					)
				}
			}
		}
		
		viewModelScope.launch(environment.dispatcher) {
			environment.getCalculationResult().collect { result ->
				setState {
					copy(
						calculationResult = result
					)
				}
			}
		}
	}
	
	override fun dispatch(action: MainAction) {
		when (action) {
			is MainAction.UpdateExpression -> {
				viewModelScope.launch(environment.dispatcher) {
					environment.setExpression(action.expression)
				}
			}
		}
	}
	
	fun getExpression(exp: String, calc: Calc): String {
		return when (calc) {
			is CalcCNumber -> exp + calc.symbol
			is CalcCOperation -> exp + calc.symbol
			is CalcCAction -> {
				when (calc) {
					is CalcCAction.Percent -> exp + calc.symbol
					is CalcCAction.Decimal -> exp + calc.symbol
					is CalcCAction.Clear -> ""
					is CalcCAction.Delete -> {
						if (exp.isNotBlank()) exp.substring(0, exp.length - 1)
						else ""
					}
					is CalcCAction.Calculate -> {
						viewModelScope.launch(environment.dispatcher) {
							environment.calculate()
						}
						
						return exp
					}
				}
			}
			else -> exp
		}
	}
	
}