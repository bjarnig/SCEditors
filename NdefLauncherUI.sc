NdefLauncherUI {

	*new {|ndefs, slots=8, clock|
		^super.new.display(ndefs, slots, clock)
	}

	*help {
		"Ndef Launcher".postln;
	}

	display {|ndefList, slots, tempoClock|

	var win, color, alpha, stringColor, defaultFont, headerFont, synth, totalDuration, offset;
	var createButton, createStateButton, createKnob, createNumber, createLabel;
	var offColor = Color.fromHexString("#ff6347");
	var onColor = Color.fromHexString("#00bfff");
	var clock = tempoClock ? TempoClock(130/60);
	var slotCount = slots;
	var currentIndex = 0, stateButtons = List(slotCount);
	var ndefs = ndefList;
	var ndefKeys = ndefs.keys.asArray.sort;
	var currentndefs = Array.newClear(slotCount);
	var lblStartX = 33, lblOffsetX = 90;
	var btnStartX = 20, btnOffsetX = 90;
	var buttons = Array.new(slotCount);

	// Common values
	color = Color.cyan(0.8);
	stringColor = Color.white;
	defaultFont =  Font("Monaco", 10);
	headerFont =  Font("Monaco", 14);
	alpha = 0.95;
	totalDuration=100;
	offset=250;

	// TODO, remove slot count and make all dynamic.
	slotCount.do {|i|
		buttons.add(List.new(slotCount));
	};

	// Add the window
	win = Window.new("- ndef Launcher -", Rect(20, 200, ndefList.size * 100, 450), scroll: false);
	win.front;
	win.background = Color.fromHexString("#071E22");
	win.alpha = alpha;
	win.drawFunc = {
		Pen.strokeColor = Color.white;
		Pen.moveTo(20@45);
		Pen.lineTo(1100@45);
		Pen.stroke;
		Pen.moveTo(20@520);
		Pen.lineTo(1100@520);
		Pen.stroke;
	};

	// Factory for labels
	createLabel = {|left, top=240, text, align = \center, color|
		var label = StaticText.new(win, Rect(left, top, 200, 25));
		label.string = text;
		label.align = align;
		label.font = headerFont;
		label.stringColor = color;
	};

	// Factory for scene selction buttons
	createStateButton = {|left, top, text, index, buttonList, slot|
		var button = Button(win, Rect(left, top, 20, 40));
		button.font = defaultFont;
		button.states = [
				["", Color.black, onColor],
				["", Color.black, offColor]
			];
		stateButtons.add(button);
	};

	// Factory for buttons
	createButton = {|left,top, text, index, buttonList, slot, pbinds|
		var button, value, currentndef;
		button = Button(win, Rect(left, top, 75, 40));
		button.font = defaultFont;
		button.states = [
		[text, Color.black, onColor],
		["Stop", Color.black, offColor]];

		button.action = {arg state;

			if(currentndefs[slot].notNil, {
				currentndefs[slot].stop;
			});

			if(state.value == 1, {
				currentndefs[slot] = pbinds[index].play();
			}, {
				currentndefs[slot].stop;
			});

			buttonList.do{|btn, i| if(i != index, {btn.value = 0})};
		};

		button;
	};

	ndefKeys.size.do {|n|
		var labelX = lblStartX + (n * lblOffsetX);

		createLabel.value(labelX, 20, ndefKeys[n], \left, stringColor );

		ndefs[ndefKeys[n]].size.do {|i|
			var index = i + 1;
			var slotY = 45 * index;
			var slotX = btnStartX + (btnOffsetX * n);
			var lbl = ndefKeys[n] + index;
			var btn = createButton.value(slotX, 15 + slotY, lbl, i, buttons[n], n, ndefs[ndefKeys[n]]);
			buttons[n].add(btn);
		};
	};

	}
}
