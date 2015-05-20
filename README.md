# FieldedSDM
Fielded Sequential Dependence Model is retrieval model for efficint searches in Web of Data. It is an expansion of [Sequential Dependence Model](http://www-dev.ccs.neu.edu/home/yzsun/classes/2014Spring_CS7280/Papers/Probabilistic_Models/A%20Markov%20Random%20Field%20Model%20for%20Term%20Dependencies.pdf) by Metzler and Croft.

All experiments were performed using [Galago](http://sourceforge.net/p/lemur/galago/ci/default/tree/).
Models are implemented as Galago traversals. You can find them in `galago` directory.
Please see [documentation](http://sourceforge.net/p/lemur/wiki/Galago%20Traversals/#implementing-your-own-traversal)
to understand how to configure Galago to use them.

We provide retrieval results of our models and baselines we used in paper in `runs` directory.

# Authors
- [Nikita Zhiltsov](https://github.com/nzhiltsov) *Kazan Federal University*, [*Textocat*](http://textocat.com/)
- [Alexander Kotov](http://www.cs.wayne.edu/kotov/) *Wayne State University*
- [Fedor Nikolaev](https://github.com/fsqcds) *Wayne State University*

# Acknowledgment
This project was partially funded by the [Kazan Federal University](http://kpfu.ru/eng).

# License
Licensed under the Apache License, Version 2.0: http://www.apache.org/licenses/LICENSE-2.0

# Citation
Please cite the following paper if you use our code or ideas in your work:

N. Zhiltsov, A. Kotov, and F. Nikolaev. *Fielded Sequential Dependence Model for Ad-Hoc Entity Retrieval in the Web of Data*  (to be published in ACM SIGIR, 2015).
