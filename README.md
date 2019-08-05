# SCEditors

Initialise the editors once working with synths and patterns for experimenting with values.

```javascript

/* Install the editors */

Quarks.install("https://github.com/bjarnig/SCEditors")

![alt text](https://bjarnig.s3.eu-central-1.amazonaws.com/images/sceditors.jpg)


///////////////////////  SynthDesignerUI //////////////////////

// The SynthDesignerUI uses a synth, a list of specs and pattern to explore the synth and tweak values.

(
	var synth = \gbman, specs = Dictionary();

	SynthDef(synth, {
		|out=0, amp=0.3, freq=100, pan=0.0, atk=1.1, dec=0.5, sus=1.0, rel=0.5, gate=1, mod=0.5, lop=18000, loprq=1|
		var env, sig;
		env = EnvGen.kr(Env.adsr(atk, dec, sus, rel), gate, doneAction:2);
		sig = Saw.ar(Lag.ar(GbmanL.ar(freq/4).range(freq/2,freq)));
		sig = BBandStop.ar(sig, freq * 2, 2);
		sig = RLPF.ar(sig, lop, loprq);
		sig = Pan2.ar(sig * env, pan) * amp;
		OffsetOut.ar(out, sig);
	}).add();

	specs[synth] = [
	[\atk, ControlSpec(0.001, 2.0, \lin, 0.001, 0.05)],
	[\sus, ControlSpec(0.001, 2.0, \lin, 0.001, 0.05)],
	[\rel, ControlSpec(0.001, 2.0, \lin, 0.001, 0.05)],
	[\freq, ControlSpec(20, 4000, \lin, 0.1, 100)],
	[\mod, ControlSpec(0.0, 1.0, \lin, 0.01, 100)],
	[\lop, ControlSpec(40, 18000, \lin, 1, 100)],
	[\amp, ControlSpec(0.0, 2.0, \lin, 0.001, 0.5)]];

	SynthDesignerUI(synth, [], specs[synth],
		[
			(name: "quite regular", args:[\dur, Pwhite(0.5, 1.0)]),
			(name: "up and down", args:[\dur, Pseq([Pseries(0.05, 0.05, 10), Pseries(1, -0.05, 10)], inf)]),
			(name: "brownian", args:[\dur, Pbrown(0.05, 0.2, 0.001)]),
		]
	)
)

////////////////////  PatternInteractionUI ////////////////////

// The PatternInteractionUI loads a pattern, specs and arguments to tweak pattern settings.

(
	var item = ();

	item.specs = [
		[\amp, ControlSpec(0.001, 4.0, \exp, 0.001, 1)],
		[\rate, ControlSpec(0.1, 8, \lin, 0.01, 1)],
		[\mod, ControlSpec(0.001, 8, \lin, 0.001, 1)],
		[\dur, ControlSpec(0.1, 8, \lin, 0.1, 1)]
	];

	item.arguments = Dictionary();
	item.specs.do{|sp| item.arguments[sp[0]] = sp[1].default };

	item.pattern = PbindProxy(
		\instrument, \gbman,
		\atk, 0.21, \dec, 2.5, \sus, 0.5, \rel, 0.2, \modp, 44, \ampp, 1, \durp, 1,
		\freqp, [2423, 1650], \lpf, 1600,  \curve, 0, \out, 0,
		\ampModAmt, 0.99, \numHarm, 2,  \ampModFreq, 28.1,
		\rate, Pfunc { item.arguments[\rate] },
		\durc, Pfunc { item.arguments[\dur] },
		\ampc, Pfunc { item.arguments[\amp] },
		\modc, Pfunc { item.arguments[\mod] },
		\freq, Pkey(\freqp) * Pkey(\rate),
	    \modFreq, Pkey(\modc) * Pkey(\modp),
		\dur, Pkey(\durp) * Pkey(\durc),
		\amp, Pkey(\ampc) * Pkey(\ampp)
	);

	PatternInteractionUI(item, Server.default);
)


///////////////////////  PatternLauncherUI ///////////////////////

// The PatternLauncherUI loads a pattern dictionary and creates a grid of buttons to switch between patterns.

(

var patterns = Dictionary(), r = List(), t = List(), q = List();

r.add(PatternProxy(Pbind(\instrument, \gbman, \sus, 0.62, \modFreq, 24, \dec, 0.29, \curve, 0.5, \freq, 623, \amp, 0.1, \dur, 0.1)));
r.add(PatternProxy(Pbind(\instrument, \gbman, \sus, 0.62, \modFreq, 14, \dec, 0.29, \curve, 0.5, \freq, 123, \amp, 0.1, \dur, 0.2)));
r.add(PatternProxy(Pbind(\instrument, \gbman, \sus, 0.62, \modFreq, 44, \dec, 0.29, \curve, 0.5, \freq, 423, \amp, 0.1, \dur, 0.3)));
r.add(PatternProxy(Pbind(\instrument, \gbman, \sus, 0.62, \modFreq, 54, \dec, 0.29, \curve, 0.5, \freq, 223, \amp, 0.1, \dur, 0.4)));
r.add(PatternProxy(Pbind(\instrument, \gbman, \sus, 0.62, \modFreq, 74, \dec, 0.29, \curve, 0.5, \freq, 423, \amp, 0.1, \dur, 0.5)));
r.add(PatternProxy(Pbind(\instrument, \gbman, \sus, 0.62, \modFreq, 84, \dec, 0.29, \curve, 0.5, \freq, 623, \amp, 0.1, \dur, 0.1)));
r.add(PatternProxy(Pbind(\instrument, \gbman, \sus, 0.62, \modFreq, 94, \dec, 0.29, \curve, 0.5, \freq, 123, \amp, 0.1, \dur, 0.2)));
r.add(PatternProxy(Pbind(\instrument, \gbman, \sus, 0.62, \modFreq, 34, \dec, 0.29, \curve, 0.5, \freq, 223, \amp, 0.1, \dur, 0.3)));
patterns["rhythms"] = r;

t.add(PatternProxy(Pbind(\instrument, \gbman, \sus, 0.62, \modFreq, 24, \dec, 0.29, \curve, 0.5, \freq, 623, \amp, 0.1, \dur, 0.1)));
t.add(PatternProxy(Pbind(\instrument, \gbman, \sus, 0.62, \modFreq, 14, \dec, 0.29, \curve, 0.5, \freq, 123, \amp, 0.1, \dur, 0.2)));
t.add(PatternProxy(Pbind(\instrument, \gbman, \sus, 0.62, \modFreq, 44, \dec, 0.29, \curve, 0.5, \freq, 423, \amp, 0.1, \dur, 0.3)));
t.add(PatternProxy(Pbind(\instrument, \gbman, \sus, 0.62, \modFreq, 54, \dec, 0.29, \curve, 0.5, \freq, 223, \amp, 0.1, \dur, 0.4)));
t.add(PatternProxy(Pbind(\instrument, \gbman, \sus, 0.62, \modFreq, 74, \dec, 0.29, \curve, 0.5, \freq, 423, \amp, 0.1, \dur, 0.5)));
t.add(PatternProxy(Pbind(\instrument, \gbman, \sus, 0.62, \modFreq, 84, \dec, 0.29, \curve, 0.5, \freq, 623, \amp, 0.1, \dur, 0.1)));
t.add(PatternProxy(Pbind(\instrument, \gbman, \sus, 0.62, \modFreq, 94, \dec, 0.29, \curve, 0.5, \freq, 123, \amp, 0.1, \dur, 0.2)));
t.add(PatternProxy(Pbind(\instrument, \gbman, \sus, 0.62, \modFreq, 34, \dec, 0.29, \curve, 0.5, \freq, 223, \amp, 0.1, \dur, 0.3)));
patterns["drones"] = t;

q.add(PatternProxy(Pbind(\instrument, \gbman, \sus, 0.62, \modFreq, 24, \dec, 0.29, \curve, 0.5, \freq, 623, \amp, 0.1, \dur, 0.1)));
q.add(PatternProxy(Pbind(\instrument, \gbman, \sus, 0.62, \modFreq, 14, \dec, 0.29, \curve, 0.5, \freq, 123, \amp, 0.1, \dur, 0.2)));
q.add(PatternProxy(Pbind(\instrument, \gbman, \sus, 0.62, \modFreq, 44, \dec, 0.29, \curve, 0.5, \freq, 423, \amp, 0.1, \dur, 0.3)));
q.add(PatternProxy(Pbind(\instrument, \gbman, \sus, 0.62, \modFreq, 54, \dec, 0.29, \curve, 0.5, \freq, 223, \amp, 0.1, \dur, 0.4)));
q.add(PatternProxy(Pbind(\instrument, \gbman, \sus, 0.62, \modFreq, 74, \dec, 0.29, \curve, 0.5, \freq, 423, \amp, 0.1, \dur, 0.5)));
q.add(PatternProxy(Pbind(\instrument, \gbman, \sus, 0.62, \modFreq, 84, \dec, 0.29, \curve, 0.5, \freq, 623, \amp, 0.1, \dur, 0.1)));
q.add(PatternProxy(Pbind(\instrument, \gbman, \sus, 0.62, \modFreq, 94, \dec, 0.29, \curve, 0.5, \freq, 123, \amp, 0.1, \dur, 0.2)));
q.add(PatternProxy(Pbind(\instrument, \gbman, \sus, 0.62, \modFreq, 34, \dec, 0.29, \curve, 0.5, \freq, 223, \amp, 0.1, \dur, 0.3)));
patterns["noises"] = q;

patterns["rhythm2"] = r; patterns["noise2"] = q; patterns["drone2"] = t;

PatternLauncherUI(patterns);

)

```
