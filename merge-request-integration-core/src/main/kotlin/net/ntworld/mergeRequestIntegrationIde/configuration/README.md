## Notes

I'm going to refactor everything to MVP architecture and DDD style.

Each module is a separated domain (DDD), each domain can have a sub-domain.

For now these are the domain:

- configuration: For configuration views
- mergeRequest : For merge request views -> this is big domain
    - Comment  : sub-domain of merge Request
    - ...
- diff         : domain for everything related to diff view
- home         : domain for home tab 

Each domain has:

- model
- view
- presenter
- util          (if needed)
- internal      (if needed)
- entity        (if needed)
- vos           (if needed)
- ...

