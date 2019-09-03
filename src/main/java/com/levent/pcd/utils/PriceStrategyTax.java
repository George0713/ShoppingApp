package com.levent.pcd.utils;

import java.util.List;

import org.springframework.stereotype.Component;

import com.levent.pcd.model.ShoppingCartEntry;

@Component
public class PriceStrategyTax implements PriceStrategy {

	@Override
	public double getTotal(List<ShoppingCartEntry> shoppingCartEntries) {
		double total = 0.0;

		for (ShoppingCartEntry e : shoppingCartEntries) {
			total += e.getProductTotalPrice() * 0.18;
		}

		return total;
	}

	public double gePriceAfterTax(double sum) {
		return Double.parseDouble(PriceUtilities.roundToTwoDecimalPlaces(sum * 1.18));
	}

}
